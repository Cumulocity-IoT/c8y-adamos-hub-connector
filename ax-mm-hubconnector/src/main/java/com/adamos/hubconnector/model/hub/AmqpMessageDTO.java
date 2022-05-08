package com.adamos.hubconnector.model.hub;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmqpMessageDTO {
	private String uuid;
	private DateTime timestampCreated;
	
	private DateTime timestampReceived;
	private String eventRootId;
	private String eventCorrelationId;
	private String eventVersion;
	private String eventCode;
	private String referenceObjectType;
	private String referenceObjectId;
}

