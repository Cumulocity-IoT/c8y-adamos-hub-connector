package com.adamos.hubconnector.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.events.AdamosEventProcessor;
import com.adamos.hubconnector.model.events.EventDirection;
import com.adamos.hubconnector.model.events.EventRule;
import com.adamos.hubconnector.model.events.EventRules;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
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

	@Autowired
	private InventoryApi inventoryApi;
	
	@Autowired
	private CumulocityService cumulocityService;

	@Autowired
	private HubConnectorService hubConnectorService;
	
    @Autowired
    private MicroserviceSubscriptionsService service;
	
	@Value("${C8Y.tenant}")
    private String tenant;	
	 
	private void debugInfo(String message) {
		if (appLogger.isDebugEnabled()) {
			appLogger.debug(message);
		}
	}
	
	public boolean consumeHubMessage(String message, DocumentContext jsonContext) throws JsonParseException, JsonMappingException, IOException {
		debugInfo("Consuming message: " + message);
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
		cumulocityService.createManagedObjectIfNotExists(CustomProperties.HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE, new EventRules(EventDirection.FROM_HUB));
		cumulocityService.createManagedObjectIfNotExists(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE, new EventRules(EventDirection.TO_HUB));
	}
	
}
