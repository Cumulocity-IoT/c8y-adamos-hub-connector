package com.adamos.hubconnector.amqp;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.hub.AmqpMessageDTO;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.adamos.hubconnector.services.CumulocityService;
import com.adamos.hubconnector.services.HubConnectorService;
import com.adamos.hubconnector.services.HubService;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

public class HubStandardMachineEventHandler extends HubStandardEventHandler {

    private HubConnectorService hubConnectorService;

    public HubStandardMachineEventHandler(CumulocityService cumulocityService, HubService hubService, HubConnectorService hubConnectorService) {
        super(cumulocityService, hubService);
        this.hubConnectorService = hubConnectorService;
    }

    @Override
    public void handleEvents(final AmqpMessageDTO message) {
        if (message.getEventCode().equals(this.getUpdateEventCode())) {
            if (hubService.checkAndUpdateDevice(message.getReferenceObjectId())) {
                LOGGER.info("Updated {} '{}'.", this.getObjectShortName(), message.getReferenceObjectId());
            }
        } else if (message.getEventCode().equals(this.getDeleteEventCode())) {
            final HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();
    
            if (globalSettings != null && globalSettings.getDefaultSyncConfiguration().getHubToAdamos().isDelete()) {
                LOGGER.info("Deleted {} '{}'.", this.getObjectShortName(), message.getReferenceObjectId());
                hubService.deleteDeviceInC8Y(message.getReferenceObjectId());
            }
        } else if (message.getEventCode().equals(this.getCreateEventCode())) {
            final HubConnectorGlobalSettings globalSettings2 = hubConnectorService.getGlobalSettings();
            if (globalSettings2 != null && globalSettings2.getDefaultSyncConfiguration().getHubToAdamos().isCreate()) {
                if (hubService.importHubDevice(message.getReferenceObjectId(),
                        globalSettings2.getDefaultSyncConfiguration().getHubToAdamos().isC8yIsDevice()) != null) {
                            LOGGER.info("Created {} '{}'.", this.getObjectShortName(), message.getReferenceObjectId());
                }
            }
        } else {
            LOGGER.warn("Could not handle {} event '{}'.", this.getObjectShortName(), message.getEventCode());
        }
    }

    @Override
    public ManagedObjectRepresentation importHubData(String uuid) {
        // Currently not needed for this special case
        return null;
    }

    @Override
    public MDMObjectDTO getObject(String uuid) {
        // Currently not needed for this special case
        return null;
    }

    @Override
    public String getUpdateEventCode() {
        return "adamos:masterdata:event:resource:machine:*:updated:1";
    }

    @Override
    public String getDeleteEventCode() {
        return "adamos:masterdata:event:resource:machine:*:deleted:1";
    }

    @Override
    public String getCreateEventCode() {
        return "adamos:masterdata:event:resource:machine:*:created:1";
    }

    @Override
    public String getObjectShortName() {
        return "machine";
    }

    @Override
    public String getObjectIdentificationType() {
        return CustomProperties.Machine.IDENTITY_TYPE;
    }
    
}