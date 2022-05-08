package com.adamos.hubconnector.polling;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.HubConnectorSettings;
import com.adamos.hubconnector.services.HubConnectorService;
import com.adamos.hubconnector.services.HubService;
import com.adamos.hubconnector.services.MigrationService;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.google.common.base.Strings;

@Service
public class ManagedObjectsHandlerService {
	
    private final Logger LOGGER = LoggerFactory.getLogger(ManagedObjectsHandlerService.class);
    
	@Value("${C8Y.tenant}")
    private String tenant;
	
    @Autowired
    private MicroserviceSubscriptionsService service;
    
	@Autowired
	InventoryApi inventoryApi;
	
	@Autowired
	HubService hubService;
	
	@Autowired
	HubConnectorService hubConnectorService;

	public void handleSubscriptionData(HashMap subscriptionData) {
		if (!MigrationService.isMigrationRunning()) {
			service.runForTenant(tenant, () -> {
				String action = subscriptionData.get("realtimeAction").toString().toLowerCase();
	
				long deviceId = -1;
				boolean isDevice = false;
				HashMap data = null;
				
				if (action.equals("delete")) {
					deviceId = Long.parseLong(subscriptionData.get("data").toString());
					isDevice = hubService.isDevice(deviceId);
				} else {
					data = (HashMap)subscriptionData.get("data");
					deviceId = Long.parseLong(data.get("id").toString());
					isDevice = (data.get(CustomProperties.C8Y.IS_DEVICE) != null) ? true : false;
				}
					
				if (isDevice) {
	
					HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();
	
				switch (action) {
					case "create":
						LOGGER.info("deviceCreated " + deviceId);
						if (globalSettings != null && globalSettings.getDefaultSyncConfiguration().getAdamosToHub().isCreate()) {
							ManagedObjectRepresentation device = inventoryApi.get(GId.asGId(deviceId));
							if (!device.hasProperty(CustomProperties.HUB_CONNECTOR_SETTINGS)) {
								LOGGER.info("Cumulocity -> Hub - create device: " + deviceId);
								hubService.createMachineTool(device);
							}
						}
						
						break;
					case "delete":
						ManagedObjectRepresentation device = inventoryApi.get(GId.asGId(deviceId));
						HubConnectorSettings settings = hubService.getConnectorSettingsByObj(device);						
						if (globalSettings != null && globalSettings.getDefaultSyncConfiguration().getAdamosToHub().isDelete() && !Strings.isNullOrEmpty(settings.getUuid())) {
							LOGGER.info("Cumulocity -> Hub - delete device: " + deviceId);
							if (hubService.deleteDeviceInHub(settings.getUuid())) {
								LOGGER.info("deviceDeleted " + deviceId);
							}
						}
	
						break;
					case "update":
						LOGGER.info("deviceModified " + deviceId);						
						ManagedObjectRepresentation device2 = inventoryApi.get(GId.asGId(deviceId));
						HubConnectorSettings settings2 = hubService.getConnectorSettingsByObj(device2);						
						if (settings2 != null && settings2.getSyncConfiguration().isSyncToHub() && !Strings.isNullOrEmpty(settings2.getUuid())) {
							if (hubService.checkAndUpdateDevice(device2, false, false)) {
								LOGGER.info("Cumulocity -> Hub - update device: " + deviceId);
							}
						}					
						
						break;
					default:
						LOGGER.error("UnknownRealtimeAction " + deviceId);
						break;
					}
				}
	
			});

		}

	}
}
