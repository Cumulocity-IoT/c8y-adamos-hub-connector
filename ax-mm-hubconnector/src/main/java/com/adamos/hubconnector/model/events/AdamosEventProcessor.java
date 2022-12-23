package com.adamos.hubconnector.model.events;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamos.hubconnector.model.hub.AmqpMessageDTO;
import com.adamos.hubconnector.services.CumulocityService;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jayway.jsonpath.DocumentContext;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
//@JsonTypeName("AdamosEventProcessor")
//@JsonTypeInfo(use = Id.NAME)
public class AdamosEventProcessor extends EventProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(HubEventTrigger.class);
	private AdamosEventChannel channel = AdamosEventChannel.EVENTS;					// defines the event-channel
	private AdamosProcessingMode processingMode = AdamosProcessingMode.PERSISTENT;	// defines the processing-mode
	
	@JsonIgnore(value=true)
	private CumulocityService cumulocityService;
	
	@Override
	public boolean processMessage(String payloadAsJson, EventRule rule) {
    	try {
    		// Incoming Message is an HubAMQP-Message
			AmqpMessageDTO amqpMessage = MAPPER.readValue(payloadAsJson, AmqpMessageDTO.class);
			
			// To be able to use jsonPath we need the DocumentContext
			DocumentContext messageContext = getJsonContext(payloadAsJson);
			
			// Every event has a referenceId to an device in Hub -> if the device is connected/mapped to this tenant we will find it
	    	ManagedObjectRepresentation device = cumulocityService.getDeviceByHubUuid(amqpMessage.getReferenceObjectId());
	    	
			if (device != null) {
				// To be able to use jsonPath on the deviceContext we have to parse it
		    	DocumentContext deviceContext = getJsonContext(device.toJSON());
		    	
		    	// After that we combine generate an Json-Output based on the template and the Context of the message and the device
		    	String customJson = this.getParsedJson(rule, deviceContext, messageContext);
		    	
		    	// depending on the eventChannel we POST the Json-Object to the correct endpoint
				switch (rule.getEventProcessor().getChannel()) {
					case EVENTS:
						this.triggerCumulocityEvent(customJson, rule);
						break;
					case ALARMS:
						this.triggerCumulocityAlarm(customJson, rule);
						break;
					case COMMANDS:
						LOGGER.debug("Detected new command based on rule " + rule.getId());
						LOGGER.warn("COMMANDS are not implemented");
						break;
					case MEASUREMENTS:
						this.triggerCumulocityMeasurement(customJson, rule);
						break;
				}
				
				// Stop processing for this kind of message
				return true;
			}
    	} catch (JsonParseException|JsonMappingException ex) {
    		LOGGER.error("Could not typecast message: " + payloadAsJson, ex);
		} catch (IOException e) {
			LOGGER.error("Error while typecasting message: " + payloadAsJson, e);
		}
    	
    	return false;
	}

	@Override
	public String getDefaultJsonTemplate(String message) {
		return "{\n\"source\": { \"id\": {{$id}} } \n}";
	}
	
	private MeasurementRepresentation triggerCumulocityMeasurement(String customJson, EventRule rule) {
		LOGGER.debug("Detected new measurement based on rule " + rule.getId());
        LOGGER.debug("Creating measurement: " + customJson);
		return cumulocityService.createMeasurement(customJson, rule.getEventProcessor().getProcessingMode());
	}
	
	private AlarmRepresentation triggerCumulocityAlarm(String customJson, EventRule rule) {
		LOGGER.debug("Detected new alarm based on rule " + rule.getId());
        LOGGER.debug("Creating alarm: " + customJson);
		return cumulocityService.createAlarm(customJson, rule.getEventProcessor().getProcessingMode());
	}
	
	private EventRepresentation triggerCumulocityEvent(String customJson, EventRule rule) {
		LOGGER.debug("Detected new event based on rule " + rule.getId());
        LOGGER.debug("Creating event: " + customJson);
		return cumulocityService.createEvent(customJson, rule.getEventProcessor().getProcessingMode());
	}
	
}
