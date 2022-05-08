package com.adamos.hubconnector.amqp;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.adamos.hubconnector.services.CumulocityService;
import com.adamos.hubconnector.services.HubService;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

public class HubStandardAreaEventHandler extends HubStandardEventHandler {

    public HubStandardAreaEventHandler(CumulocityService cumulocityService, HubService hubService) {
        super(cumulocityService, hubService);
    }

    @Override
    public ManagedObjectRepresentation importHubData(String uuid) {
        return hubService.importHubArea(uuid);
    }

    @Override
    public String getUpdateEventCode() {
        return "adamos-1:mdm:event:areaModified:1";
    }

    @Override
    public String getDeleteEventCode() {
        return "adamos-1:mdm:event:areaDeleted:1";
    }

    @Override
    public String getCreateEventCode() {
        return "adamos-1:mdm:event:areaCreated:1";
    }

    @Override
    public String getObjectShortName() {
        return "area";
    }

    @Override
    public String getObjectIdentificationType() {
        return CustomProperties.Area.IDENTITY_TYPE;
    }

    @Override
    public MDMObjectDTO getObject(String uuid) {
        return hubService.getArea(uuid);
    }
    
}