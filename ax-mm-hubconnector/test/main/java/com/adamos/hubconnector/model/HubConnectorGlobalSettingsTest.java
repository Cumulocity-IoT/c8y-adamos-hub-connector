package com.adamos.hubconnector.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HubConnectorGlobalSettingsTest {

	@Test
	void testGetOAuthEndpoint() {
		HubConnectorGlobalSettings globalSettings = new HubConnectorGlobalSettings();
		globalSettings.setEnvironment("adamos-hub.dev");
		assertEquals("https://services.adamos-hub.dev/auth-service/token", globalSettings.getOAuthEndpoint());
	}

	
	@Test
	void testGetAdamosMdmServiceEndpoint() {
		HubConnectorGlobalSettings globalSettings = new HubConnectorGlobalSettings();
		globalSettings.setEnvironment("adamos-hub.dev");
		assertEquals("https://services.adamos-hub.dev/mdm-service/v1/", globalSettings.getAdamosMdmServiceEndpoint());
	}

	@Test
	void testGetAdamosCatalogServiceEndpoint() {
		HubConnectorGlobalSettings globalSettings = new HubConnectorGlobalSettings();
		globalSettings.setEnvironment("adamos-hub.dev");
		assertEquals("https://services.adamos-hub.dev/catalog-service/v1/", globalSettings.getAdamosCatalogServiceEndpoint());
	}

	@Test
	void testGetAdamosEventServiceEndpoint() {
		HubConnectorGlobalSettings globalSettings = new HubConnectorGlobalSettings();
		globalSettings.setEnvironment("adamos-hub.dev");
		assertEquals("https://services.adamos-hub.dev/event-service/v1/", globalSettings.getAdamosEventServiceEndpoint());
	}

	@Test
	void testGetAdamosAmqpEndpoint() {
		HubConnectorGlobalSettings globalSettings = new HubConnectorGlobalSettings();
		globalSettings.setEnvironment("adamos-hub.dev");
		assertEquals("amqps://messaging.adamos-hub.dev", globalSettings.getAdamosAmqpEndpoint());
	}

}
