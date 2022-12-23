package com.adamos.hubconnector.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

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
import com.adamos.hubconnector.model.events.AdamosEventAttribute;
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
		EventMapping mapping = mapping().id("m123").c8yEventType("cet123").adamosEventType("aet123").build();
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
					assertNull(data.getAttributes());
					return true;
					}),
				eq(AdamosEventData.class)
				);
	}

	@Test
	void testMapEventsOneMatchingFragment() throws RestClientException, URISyntaxException {
		EventMapping mapping = mapping().id("m123").c8yEventType("cet123").adamosEventType("aet123").fragments(Collections.singletonList("f1")).build();
		HubConnectorGlobalSettings settings = new HubConnectorGlobalSettings(true);
		settings.setEnvironment("adamos-hub.dev");
		ManagedObjectRepresentation device = device("d123");
		EventRepresentation event = event(mapping.getC8yEventType(), device);
		event.set(attribute("t1", "s1", "v1"), "f1");
		when(
				cumulocityService.getEvents(eq(device), eq(mapping.getC8yEventType()), any())
		).thenReturn(
				Collections.singletonList(event)
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
					assertArrayEquals(new AdamosEventAttribute[] {attribute("t1", "s1", "v1")}, data.getAttributes());
					return true;
					}),
				eq(AdamosEventData.class)
				);
	}
	
	private EventMappingBuilder mapping() {
		return new EventMappingBuilder();
	}

	private static class EventMappingBuilder {
		
		private String id;
		private String c8yEventType;
		private String adamosEventType;
		private List<String> fragments = Collections.emptyList();

		public EventMapping build() {
			EventMapping mapping = new EventMapping();
			mapping.setId(this.id);
			mapping.setC8yEventType(this.c8yEventType);
			mapping.setAdamosEventType(this.adamosEventType);
			mapping.setC8yFragments(this.fragments);
			mapping.setEnabled(true);
			return mapping;
		}
		
		public EventMappingBuilder id(String id) {
			this.id = id;
			return this;
		}
		
		public EventMappingBuilder c8yEventType(String c8yEventType) {
			this.c8yEventType = c8yEventType;
			return this;
		}

		public EventMappingBuilder adamosEventType(String adamosEventType) {
			this.adamosEventType = adamosEventType;
			return this;
		}

		public EventMappingBuilder fragments(List<String> fragments) {
			this.fragments = fragments;
			return this;
		}

	}
	
	private AdamosEventAttribute attribute(String attributeTypeId, String state, String value) {
		AdamosEventAttribute attr = new AdamosEventAttribute();
		attr.setAttributeTypeId(attributeTypeId);
		attr.setState(state);
		attr.setValue(value);
		return attr;
	}
	
	private EventRepresentation event(String type, ManagedObjectRepresentation source) {
		EventRepresentation event = new EventRepresentation();
		event.setId(new GId());
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
