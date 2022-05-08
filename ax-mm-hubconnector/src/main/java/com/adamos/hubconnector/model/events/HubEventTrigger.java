package com.adamos.hubconnector.model.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamos.hubconnector.model.hub.AmqpMessageDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Strings;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class HubEventTrigger {
	private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JodaModule());
	private static final Logger LOGGER = LoggerFactory.getLogger(HubEventTrigger.class);
	
	private String eventCode;
	private String referenceObjectType;
	private List<String> referenceObjectIds;
	
	public HubEventTrigger() {
		this.referenceObjectIds = new ArrayList<String>();
	}

	public boolean doesMatch(String payloadAsJson) {
    	try {
			AmqpMessageDTO amqpMessage = MAPPER.readValue(payloadAsJson, AmqpMessageDTO.class);
			
			return matchByPattern(this.getReferenceObjectType(), amqpMessage.getReferenceObjectType()) &&
					matchByPattern(this.getEventCode(), amqpMessage.getEventCode());
    	} catch (JsonParseException|JsonMappingException ex) {
    		LOGGER.error("Could not typecast message: " + payloadAsJson, ex);
		} catch (IOException e) {
			LOGGER.error("Error while typecasting message: " + payloadAsJson, e);
		}
    	
		return false;
	}
	
	private boolean matchByPattern(String searchPattern, String searchText) {
		if (!Strings.isNullOrEmpty(searchPattern)) {
			Pattern pattern = Pattern.compile(searchPattern);
			Matcher matcher = pattern.matcher(searchText);
			return matcher.matches();
		}
		return true;
	}

}
