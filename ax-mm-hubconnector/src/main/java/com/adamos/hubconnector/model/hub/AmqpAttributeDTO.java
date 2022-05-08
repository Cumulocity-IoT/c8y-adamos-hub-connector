package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmqpAttributeDTO<T> {
	private String attributeTypeId;
	private String description;
	private T value;
}