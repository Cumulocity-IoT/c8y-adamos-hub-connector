package com.adamos.hubconnector.model.events;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class AdamosEventData {

	private String eventCode;
	private String referenceObjectType;
	private String referenceObjectId;
	private DateTime timestampCreated;
	private AdamosEventAttribute[] attributes;
	
}
