package com.adamos.hubconnector.model.migration;

import java.util.HashMap;

import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

import org.apache.commons.lang3.SerializationUtils;

/**
 * ConnectorSettings
 */
public class ConnectorSettingsRenameToHub implements IAdditionalObjectChanges {

    @Override
    public ManagedObjectRepresentation applyChanges(String newFragmentName, ManagedObjectRepresentation obj) {
        // This seems only to work with a deep copy of the Fragment
        HashMap<String, Object> fragment = SerializationUtils.clone((HashMap<String, Object>)obj.getProperty(newFragmentName));

        HashMap<String, Object> syncConfiguration = (HashMap<String, Object>)fragment.get("syncConfiguration");

        // Change property "syncConfiguration.syncFromXHub" to "syncConfiguration.syncFromHub"
        syncConfiguration.put("syncFromHub", syncConfiguration.get("syncFromXHub"));
        syncConfiguration.remove("syncFromXHub");

        // Change property "syncConfiguration.syncToXHub" to "syncConfiguration.syncToHub"
        syncConfiguration.put("syncToHub", syncConfiguration.get("syncToXHub"));
        syncConfiguration.remove("syncToXHub");

        obj.setProperty(newFragmentName, fragment);

        return obj;
    }

    
}