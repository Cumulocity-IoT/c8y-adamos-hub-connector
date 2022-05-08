package com.adamos.hubconnector.model.migration;

import java.util.ArrayList;
import java.util.HashMap;

import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

import org.apache.commons.lang3.SerializationUtils;

public class EventRulesRenameToHub implements IAdditionalObjectChanges {

    private boolean isFromHub;

    public EventRulesRenameToHub(boolean IsFromHub) {
        this.isFromHub = IsFromHub;
    }

    private String replaceText() {
        if (this.isFromHub) {
            return "FROM_HUB";
        }
        return "TO_HUB";
    }


    @Override
    public ManagedObjectRepresentation applyChanges(String newFragmentName, ManagedObjectRepresentation obj) {

        // This seems only to work with a deep copy of the Fragment
        HashMap<String, Object> fragment = SerializationUtils.clone((HashMap<String, Object>)obj.getProperty(newFragmentName));

        // Change property "direction" "FROM_XHUB" to "FROM_HUB" (also in each rule-object)
        fragment.put("direction", this.replaceText());
        
        if (fragment.containsKey("rules")) {
            ArrayList<HashMap<String, Object>> rules = (ArrayList<HashMap<String, Object>>)fragment.get("rules");
            for (HashMap<String,Object> rule : rules) {
                rule.put("direction", this.replaceText());
            }
        }

        // Update the original fragment with the cloned and changed version
        obj.setProperty(newFragmentName, fragment);

        return obj;
    }
    
}