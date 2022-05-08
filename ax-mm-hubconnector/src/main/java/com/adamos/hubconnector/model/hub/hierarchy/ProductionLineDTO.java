package com.adamos.hubconnector.model.hub.hierarchy;

import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
//@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper = true)
public class ProductionLineDTO extends MDMObjectDTO{
    @JsonProperty("@type")
	private String type;
    private String description;
    private int version;
    private String name;
}