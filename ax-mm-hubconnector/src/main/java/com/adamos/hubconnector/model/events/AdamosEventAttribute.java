package com.adamos.hubconnector.model.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class AdamosEventAttribute {

	String attributeTypeId;
	String state;
	String value;
	
}
