package com.adamos.hubconnector;

public abstract class CustomProperties {
	// General prefix for HubConnector
    public static final String HUB_PREFIX = "adamos_hub_";

    // Inventory Object-Types
    public static final String HUB_GLOBAL_SETTINGS = HUB_PREFIX + "globalSettings";	// Global Settings contain OAuthCredentials and Sync-Settings
    public static final String HUB_EVENTRULES_FROM_HUB_OBJECT_TYPE = HUB_PREFIX + "eventRules_FromHub";	// Ordered list of event-redirection rules from Hub to Cumulocity
    public static final String HUB_EVENTRULES_TO_HUB_OBJECT_TYPE = HUB_PREFIX + "eventRules_ToHub"; // not planned at the moment, but we are prepared...

    // Inventory Properties
    public static final String HUB_CONNECTOR_SETTINGS = HUB_PREFIX + "connectorSettings";
    public static final String HUB_DATA = HUB_PREFIX + "data";
    public static final String HUB_THUMBNAIL = HUB_PREFIX + "thumbnail";
    
    // AlarmTypes
    public static final String HUB_AMQP_ALARM = HUB_PREFIX + "alarm_amqp";

    public static class Machine {
        public static final String EQUIPMENT_MACHINETOOL = "MACHINE_TOOL";
        public static final String IDENTITY_TYPE = HUB_PREFIX + "machineTool_uuid";    	
    }
    
    public class ProductionLine {
        public static final String OBJECT_TYPE = HUB_PREFIX + "productionline";
        public static final String IDENTITY_TYPE = HUB_PREFIX + "productionline_uuid";

    }

    public static class Area {
        public static final String OBJECT_TYPE = HUB_PREFIX + "area";
        public static final String IDENTITY_TYPE = HUB_PREFIX + "area_uuid";	
    }
    
    public static class Site {
        public static final String OBJECT_TYPE = HUB_PREFIX + "site";	
        public static final String IDENTITY_TYPE = HUB_PREFIX + "site_uuid";
    }
    
    // Hub Constants
    public static final String HUB_IS_DEVICE = HUB_PREFIX + "isDevice";

    
    public static class C8Y {
        public static final String IS_DEVICE = "c8y_IsDevice";
        public static final String HARDWARE = "c8y_Hardware";    	
    }
    
}
