package com.adamos.hubconnector.model;

import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncTuple {
    private EquipmentDTO adamosDevice;
    private ManagedObjectRepresentation c8yDevice;
}
