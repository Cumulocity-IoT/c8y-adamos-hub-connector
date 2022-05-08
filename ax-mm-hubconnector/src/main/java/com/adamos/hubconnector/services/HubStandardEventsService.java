package com.adamos.hubconnector.services;

import org.springframework.stereotype.Service;

import com.adamos.hubconnector.amqp.HubStandardAreaEventHandler;
import com.adamos.hubconnector.amqp.HubStandardEventHandler;
import com.adamos.hubconnector.amqp.HubStandardMachineEventHandler;
import com.adamos.hubconnector.model.hub.AmqpMessageDTO;

@Service
public class HubStandardEventsService {
    private HubStandardAreaEventHandler areaEventHandler;
    private HubStandardMachineEventHandler machineEventHandler;

    public HubStandardEventsService(HubService hubService, HubConnectorService hubConnectorService, CumulocityService cumulocityService) {
        this.areaEventHandler = new HubStandardAreaEventHandler(cumulocityService, hubService);
        this.machineEventHandler = new HubStandardMachineEventHandler(cumulocityService, hubService, hubConnectorService);
    }

    private HubStandardEventHandler getEventHandlerByType(String referenceObjectType) {
        referenceObjectType = referenceObjectType.toLowerCase().trim();
        switch (referenceObjectType) {
            case "adamos:masterdata:type:machine:1":
                return this.machineEventHandler;
            case "adamos:masterdata:type:area:1":
                return this.areaEventHandler;
        }
        return null;
    }

    public void handleEvent(final AmqpMessageDTO message) {
        if (message.getReferenceObjectType() != null) {
            HubStandardEventHandler eventHandler = getEventHandlerByType(message.getReferenceObjectType());
            if (eventHandler != null) {
                eventHandler.handleEvents(message);
            }
        }
    }
   
}