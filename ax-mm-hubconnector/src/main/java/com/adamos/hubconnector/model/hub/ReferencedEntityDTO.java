package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper=true)
public class ReferencedEntityDTO extends MDMObjectDTO{
	
	@JsonProperty("@type")
	private String type;
	private int version;  
}
