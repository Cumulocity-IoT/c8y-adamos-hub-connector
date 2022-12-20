package com.adamos.hubconnector.services;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorResponse;
import com.adamos.hubconnector.model.events.AdamosEventData;
import com.adamos.hubconnector.model.events.AdamosEventProcessor;
import com.adamos.hubconnector.model.events.EventDirection;
import com.adamos.hubconnector.model.events.EventRule;
import com.adamos.hubconnector.model.events.EventRules;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.sdk.client.Param;
import com.cumulocity.sdk.client.QueryParam;
import com.cumulocity.sdk.client.event.EventApi;
import com.cumulocity.sdk.client.event.EventFilter;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Strings;
import com.jayway.jsonpath.DocumentContext;

@Service
public class EventRulesService {
	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());
	private static final Logger appLogger = LoggerFactory.getLogger(EventRulesService.class);

	QueryParam revertParam = new QueryParam(new Param() {
		@Override
		public String getName() {
			return "revert";
		}
	}, "true");

	@Autowired
	private InventoryApi inventoryApi;

	@Autowired
	private EventApi eventApi;

	@Autowired
	private CumulocityService cumulocityService;

	@Autowired
	private HubConnectorService hubConnectorService;

	@Autowired
	private HubService hubService;

	
    @Autowired
    private MicroserviceSubscriptionsService service;
	
	@Value("${C8Y.tenant}")
	private String tenant;

	public EventRulesService() {}
	
	/**
	 * Inbound event processing (Hub -> C8Y)
	 * 
	 * @param message
	 * @param jsonContext
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public boolean consumeHubMessage(String message, DocumentContext jsonContext)
			throws JsonParseException, JsonMappingException, IOException {
		appLogger.debug("Consuming message: " + message);
		EventRules rulesFromHub = getEventRules(EventDirection.FROM_HUB);
		for (EventRule rule : rulesFromHub.getRules()) {
			if (rule.doesMatch(message) && rule.isEnabled()) {
				AdamosEventProcessor adamosEventProcessor = (AdamosEventProcessor) rule.getEventProcessor();
				adamosEventProcessor.setHubConnectorService(hubConnectorService);
				adamosEventProcessor.setCumulocityService(cumulocityService);
				adamosEventProcessor.processMessage(message, rule);
			}
		}
		return false;
	}

	/**
	 * Outbound event processing
	 * TODO replace with Notification API 2.0 once available
	 */
	@Scheduled(fixedRate = 60000)
	public void consumeC8YEvent() {
		service.runForEachTenant(() -> {
			appLogger.info("Scheduled Hub event processing for tenant " + service.getTenant());
			Iterable<EventRepresentation> events = eventApi.getEventsByFilter(
					new EventFilter().byType("AdamosHubEvent").byFromDate(Date.from(Instant.now().minusSeconds(60))))
					.get(2000, revertParam).allPages();
			for (EventRepresentation e : events) {
				GId source = e.getSource().getId();
				ManagedObjectRepresentation mo = inventoryApi.get(source);
				if (mo.hasProperty(CustomProperties.HUB_DATA)) {
					EquipmentDTO hubData = new HubConnectorResponse<EquipmentDTO>(mo, CustomProperties.HUB_DATA,
							EquipmentDTO.class).getData();
					AdamosEventData eventData = e.get(AdamosEventData.class);
					eventData.setTimestampCreated(e.getDateTime());
					eventData.setReferenceObjectType("adamos:masterdata:type:machine:1");
					eventData.setReferenceObjectId(hubData.getUuid());
					URI uriService = UriComponentsBuilder
							.fromUriString(hubConnectorService.getGlobalSettings().getAdamosEventServiceEndpoint())
							.path("event").build().toUri();
					appLogger.info("Posting to " + uriService + ": " + eventData);
					hubService.restToHub(uriService, HttpMethod.POST, eventData, AdamosEventData.class);
				} else {
					appLogger.warn(
							"Cannot send event to Adamos Hub. Device " + source + " is not synchronized to Adamos Hub");
				}
			}
		});
	}
	
	private String getTypeByDirection(EventDirection direction) {
		switch (direction) {
			case FROM_HUB:
				return CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE;
			case TO_HUB:
				return CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE;
			default:
				return "";
		}
	}

	public EventRules getEventRules(EventDirection direction) {
		String directionType = getTypeByDirection(direction);

		if (!Strings.isNullOrEmpty(directionType)) {
			ManagedObjectRepresentation obj = cumulocityService.getManagedObjectByFragmentType(directionType);
			if (obj != null && obj.hasProperty(directionType)) {
				return mapper.convertValue(obj.getProperty(directionType), EventRules.class);
			}
		}

		return null;
	}

	public void storeEventRules(EventRules eventRules) {
		String directionType = getTypeByDirection(eventRules.getDirection());

		if (!Strings.isNullOrEmpty(directionType)) {
			ManagedObjectRepresentation obj = cumulocityService.getManagedObjectByFragmentType(directionType);
			if (obj != null && obj.hasProperty(directionType)) {
				obj.setProperty(directionType, eventRules);
				obj.setLastUpdatedDateTime(null);
				service.runForTenant(tenant, () -> {
					inventoryApi.update(obj);
				});
			}
		}
	}

	public void initMappingRules() {
		// Triggered on Microservice Subscription
		cumulocityService.createManagedObjectIfNotExists(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE,
				new EventRules(EventDirection.FROM_HUB));
		cumulocityService.createManagedObjectIfNotExists(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE,
				new EventRules(EventDirection.TO_HUB));
	}

}
