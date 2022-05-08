package com.adamos.hubconnector.model.events;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class EventRule {
	@JsonIgnore
	private EventDirection direction = EventDirection.FROM_HUB;
	private HubEventTrigger eventTrigger = new HubEventTrigger();
	private AdamosEventProcessor eventProcessor = new AdamosEventProcessor();
	private PayloadProcessingMode payloadProcessingMode = PayloadProcessingMode.ALL;
//	private List<String> selectedAttributes;
	private String output;
	private String id;
	private String name;
	private boolean isEnabled = true;
	
	public EventRule(EventDirection direction) {
		this.id = UUID.randomUUID().toString();
		this.name = "";
		this.direction = direction;
		this.eventTrigger = new HubEventTrigger();
		this.eventProcessor = new AdamosEventProcessor();
		//this.selectedAttributes = new ArrayList<String>();
		this.output = "";
	}
	
	@JsonIgnore
	public boolean doesMatch(String amqpMessage) {
		// If the ReferenceObjectType and the EventCode match -> the message has to be processed
		return eventTrigger.doesMatch(amqpMessage);
	}
	
}
