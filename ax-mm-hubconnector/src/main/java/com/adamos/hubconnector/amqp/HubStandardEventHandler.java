package com.adamos.hubconnector.amqp;

import com.adamos.hubconnector.model.hub.AmqpMessageDTO;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.adamos.hubconnector.services.CumulocityService;
import com.adamos.hubconnector.services.HubService;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HubStandardEventHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(HubStandardEventHandler.class);
    public abstract ManagedObjectRepresentation importHubData(String uuid);
    public abstract MDMObjectDTO getObject(String uuid);
    public abstract String getUpdateEventCode();
    public abstract String getDeleteEventCode();
    public abstract String getCreateEventCode();
    public abstract String getObjectShortName();
    public abstract String getObjectIdentificationType();

    protected CumulocityService cumulocityService;
    protected HubService hubService;

    public HubStandardEventHandler(CumulocityService cumulocityService, HubService hubService) {
        this.cumulocityService = cumulocityService;
        this.hubService = hubService;
    }

    public void handleEvents(final AmqpMessageDTO message) {
        MDMObjectDTO object = this.getObject(message.getReferenceObjectId());
        if (object != null) {
            if (message.getEventCode().equals(this.getUpdateEventCode())) {
                if (cumulocityService.updateHubData(object, this.getObjectIdentificationType())) {
                    LOGGER.info("Updated {} '{}'.", this.getObjectShortName(), message.getReferenceObjectId());
                }
            } else if (message.getEventCode().equals(this.getDeleteEventCode())) {
                if (cumulocityService.deleteHubObject(object, this.getObjectIdentificationType())) {
                    LOGGER.info("Deleted {} '{}'.", this.getObjectShortName(), message.getReferenceObjectId());
                }
            } else if (message.getEventCode().equals(this.getCreateEventCode())) {
                if (this.importHubData(message.getReferenceObjectId()) != null) {
                    LOGGER.info("Created {} '{}'.", this.getObjectShortName(), message.getReferenceObjectId());
                }
            } else {
                LOGGER.warn("Could not handle {} event '{}'.", this.getObjectShortName(), message.getEventCode());
            }
        } else {
            LOGGER.warn("Could not find {} '{}' in current realm.", this.getObjectShortName(), message.getReferenceObjectId());
        }
    }    

}