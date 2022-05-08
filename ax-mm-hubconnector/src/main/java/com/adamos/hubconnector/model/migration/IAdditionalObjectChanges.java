package com.adamos.hubconnector.model.migration;

import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

public interface IAdditionalObjectChanges {
    ManagedObjectRepresentation applyChanges(String newFragmentName, ManagedObjectRepresentation obj);
}