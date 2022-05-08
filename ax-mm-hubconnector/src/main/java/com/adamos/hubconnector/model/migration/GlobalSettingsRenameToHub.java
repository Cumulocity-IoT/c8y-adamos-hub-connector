package com.adamos.hubconnector.model.migration;

import java.util.HashMap;
import java.util.Map;

import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

import org.apache.commons.lang3.SerializationUtils;

public class GlobalSettingsRenameToHub implements IAdditionalObjectChanges {

    @Override
    public ManagedObjectRepresentation applyChanges(String newFragmentName, ManagedObjectRepresentation obj) {
        // This seems only to work with a deep copy of the Fragment
        HashMap<String, Object> fragment = SerializationUtils.clone((HashMap<String, Object>)obj.getProperty(newFragmentName));
        Map<String, Object> defaultSyncConfiguration = ((HashMap<String, Object>)(fragment).get("defaultSyncConfiguration"));

        // Change property "xHubToAdamos" to "hubToAdamos"
        defaultSyncConfiguration.put("hubToAdamos", ((HashMap)defaultSyncConfiguration.get("xHubToAdamos")).clone());

        // Remove property "xHubToAdamos"
        defaultSyncConfiguration.remove("xHubToAdamos");

        // Change property "adamosToXHub" to "adamosToHub"
        defaultSyncConfiguration.put("adamosToHub", ((HashMap)defaultSyncConfiguration.get("adamosToXHub")).clone());

        // Remove property "adamosToXHub"
        defaultSyncConfiguration.remove("adamosToXHub");

        // Update the original fragment with the cloned and changed version
        obj.setProperty(newFragmentName, fragment);

        return obj;
    }
    
}