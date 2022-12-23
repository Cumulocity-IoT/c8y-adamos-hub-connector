package com.adamos.hubconnector.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.events.AdamosEventData;
import com.adamos.hubconnector.model.events.EventMapping;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

class EventRulesServiceTest {

	@Mock
	CumulocityService cumulocityService;

	@Mock
	private HubConnectorService hubConnectorService;

	@Mock
	private HubService hubService;

	
	@InjectMocks
	EventRulesService ers;
	
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void testMapEventsNoDevices() {
		EventMapping mapping = new EventMapping();
		mapping.setId("123");
		ers.mapEvents(mapping, Collections.emptyList());
		verifyNoInteractions(cumulocityService);
	}

	@Test
	void testMapEventsOneDeviceOneEvent() throws RestClientException, URISyntaxException {
		EventMapping mapping = new EventMapping();
		mapping.setId("m123");
		mapping.setC8yEventType("cet123");
		mapping.setAdamosEventType("aet123");
		mapping.setC8yFragments(Collections.emptyList());
		mapping.setEnabled(true);
		HubConnectorGlobalSettings settings = new HubConnectorGlobalSettings(true);
		settings.setEnvironment("adamos-hub.dev");
		ManagedObjectRepresentation device = device("d123");
		when(
				cumulocityService.getEvents(eq(device), eq(mapping.getC8yEventType()), any())
		).thenReturn(
				Collections.singletonList(event(mapping.getC8yEventType(), device))
		);
		when(
				hubConnectorService.getGlobalSettings()
		).thenReturn(
				settings
		);
		ers.mapEvents(mapping, Collections.singletonList(device));
		verify(hubService).restToHub(
				eq(new URI("https://services.adamos-hub.dev/event-service/v1/event")),
				eq(HttpMethod.POST),
				argThat(x -> {
					AdamosEventData data = (AdamosEventData) x;
					assertEquals(mapping.getAdamosEventType(), data.getEventCode());
					assertEquals("adamos:masterdata:type:machine:1", data.getReferenceObjectType());
					assertEquals("uuid123", data.getReferenceObjectId());
					return true;
					}),
				eq(AdamosEventData.class)
				);
	}

	private EventRepresentation event(String type, ManagedObjectRepresentation source) {
		EventRepresentation event = new EventRepresentation();
		event.setType(type);
		event.setSource(source);
		event.setCreationDateTime(DateTime.now());
		return event;
	}
	
	private ManagedObjectRepresentation device(String id) {
		ManagedObjectRepresentation mo = new ManagedObjectRepresentation();
		mo.setId(GId.asGId(id));
		EquipmentDTO hubdata = new EquipmentDTO();
		hubdata.setUuid("uuid123");
		mo.set(hubdata, CustomProperties.HUB_DATA);
		return mo;
	}
}
