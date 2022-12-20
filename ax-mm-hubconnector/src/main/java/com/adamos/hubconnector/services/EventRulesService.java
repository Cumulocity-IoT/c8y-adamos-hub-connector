package com.adamos.hubconnector.services;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.svenson.util.JSONPathUtil;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorResponse;
import com.adamos.hubconnector.model.events.AdamosEventAttribute;
import com.adamos.hubconnector.model.events.AdamosEventData;
import com.adamos.hubconnector.model.events.AdamosEventProcessor;
import com.adamos.hubconnector.model.events.EventDirection;
import com.adamos.hubconnector.model.events.EventMapping;
import com.adamos.hubconnector.model.events.EventRule;
import com.adamos.hubconnector.model.events.EventRules;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
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

	private Map<String, Date> lastUpdateDatesCache = new HashMap<>();

	public EventRulesService() {
	}

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
		EventRules rulesFromHub = getEventRules();
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

	@Scheduled(fixedRate = 60000)
	public void startListenersForEventMappings() {
		service.runForEachTenant(() -> {
			appLogger.info("Scheduled Hub event processing for tenant " + service.getTenant());

			EventMapping[] mappings = getEventMapping();
			if (mappings == null || mappings.length == 0) {
				return;
			}

			final List<ManagedObjectRepresentation> hubDevices = cumulocityService
					.getManagedObjectsByFragmentType("adamos_hub_data");

			Stream<EventMapping> mappingStream = Arrays.asList(mappings).stream();
			// remove deleted mapping entries from cache
			for (String cachedId : lastUpdateDatesCache.keySet()) {
				if (mappingStream.filter(m -> m.getId().equals(cachedId)).findFirst().orElse(null) == null) {
					lastUpdateDatesCache.remove(cachedId);
				}
			}

			for (EventMapping mapping : mappings) {
				if (mapping.isEnabled()) {
					ArrayList<String> relevantHubDeviceIds = mapping.getC8yDevices();
					// filter for c8y devices which were configured in the mapping via c8yDevices
					List<ManagedObjectRepresentation> devicesFromMapping = relevantHubDeviceIds.isEmpty() ? hubDevices
							: hubDevices.stream().filter(d -> relevantHubDeviceIds.contains(d.getId().toString()))
									.collect(Collectors.toList());
					listenForMapping(mapping, devicesFromMapping);
				}
			}
		});
	}

	public void listenForMapping(EventMapping mapping, List<ManagedObjectRepresentation> selectedDevices) {
		appLogger.info("Started Hub event listener for mapping " + mapping.getName());
		Date fromDate = Date.from(Instant.now().minusSeconds(60));
		if (lastUpdateDatesCache.containsKey(mapping.getId())) {
			fromDate = lastUpdateDatesCache.get(mapping.getId());
		}

		ArrayList<EventRepresentation> allEvents = new ArrayList<>();
		for (ManagedObjectRepresentation device : selectedDevices) {
			Iterable<EventRepresentation> events = eventApi.getEventsByFilter(
					new EventFilter().bySource(device.getId()).byType(mapping.getC8yEventType()).byFromDate(fromDate))
					.get(2000, revertParam).allPages();
			events.forEach(allEvents::add);
		}

		List<AdamosEventData> mappedEvents = allEvents.stream().map(e -> mapToAdamosEvent(e, mapping, selectedDevices))
				.collect(Collectors.toList());

		// update cache with latest date of all events we fetched
		Date latestDate = allEvents.stream().map(e -> e.getCreationDateTime().toDate()).max(Date::compareTo).get();
		lastUpdateDatesCache.put(mapping.getId(), latestDate);

		mappedEvents.forEach(e -> this.createAdamosEvent(e));
	}

	private void createAdamosEvent(AdamosEventData eventData) {
		URI uriService = UriComponentsBuilder
				.fromUriString(hubConnectorService.getGlobalSettings().getAdamosEventServiceEndpoint())
				.path("event").build().toUri();
		appLogger.info("Posting to " + uriService + ": " + eventData);
		hubService.restToHub(uriService, HttpMethod.POST, eventData, AdamosEventData.class);
	}

	private AdamosEventData mapToAdamosEvent(EventRepresentation event, EventMapping mapping,
			List<ManagedObjectRepresentation> selectedDevices) {

		// get the hub uuid from the source device of the event
		ManagedObjectRepresentation device = selectedDevices.stream()
				.filter(d -> d.getId().toString().equals(event.getSource().getId().toString())).findFirst().get();
		EquipmentDTO hubData = new HubConnectorResponse<EquipmentDTO>(device, CustomProperties.HUB_DATA,
				EquipmentDTO.class).getData();
		String hubUuid = hubData.getUuid();

		AdamosEventData eventData = event.get(AdamosEventData.class);
		eventData.setTimestampCreated(event.getDateTime());
		eventData.setReferenceObjectType(mapping.getAdamosEventType());
		eventData.setReferenceObjectId(hubUuid);

		if (!mapping.getC8yFragments().isEmpty()) {
			List<Object> attributes = mapping.getC8yFragments().stream()
					.map(fragment -> mapToAdamosEventAttribute(fragment, event)).collect(Collectors.toList());
			eventData.setAttributes(attributes.toArray(new AdamosEventAttribute[attributes.size()]));
		}

		return eventData;
	}

	private List<Object> mapToAdamosEventAttribute(String c8yPropertyPath, EventRepresentation event) {
		JSONPathUtil util = new JSONPathUtil();
		Object value = util.getPropertyPath(event, c8yPropertyPath);
		// value could potentially also be an array
		return Arrays.asList(value);
	}

	public EventRules getEventRules() {
		ManagedObjectRepresentation obj = cumulocityService
				.getManagedObjectByFragmentType(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE);
		if (obj != null) {
			return mapper.convertValue(obj.getProperty(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE),
					EventRules.class);
		}

		return null;
	}

	public EventMapping[] getEventMapping() {
		ManagedObjectRepresentation obj = cumulocityService
				.getManagedObjectByFragmentType(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE);
		if (obj != null) {
			return mapper.convertValue(obj.getProperty(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE),
					EventMapping[].class);
		}
		return null;
	}

	public void storeEventRules(EventRules eventRules) {
		ManagedObjectRepresentation obj = cumulocityService
				.getManagedObjectByFragmentType(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE);
		if (obj != null) {
			obj.setProperty(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE, eventRules);
			obj.setLastUpdatedDateTime(null);
			service.runForTenant(tenant, () -> {
				inventoryApi.update(obj);
			});
		}

	}

	public void updateEventMapping(EventMapping[] eventMapping) {
		ManagedObjectRepresentation obj = cumulocityService
				.getManagedObjectByFragmentType(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE);
		if (obj != null) {
			obj.setProperty(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE, eventMapping);
			obj.setLastUpdatedDateTime(null);
			service.runForTenant(tenant, () -> {
				inventoryApi.update(obj);
			});
		}
	}

	public void initMappingRules() {
		// Triggered on Microservice Subscription
		cumulocityService.createManagedObjectIfNotExists(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE,
				new EventRules(EventDirection.FROM_HUB));
		cumulocityService.createManagedObjectIfNotExists(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE,
				new EventMapping[0]);
	}

}
