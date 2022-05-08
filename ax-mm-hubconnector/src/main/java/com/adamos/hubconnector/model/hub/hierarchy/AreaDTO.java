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
public class AreaDTO extends MDMObjectDTO {
    private String description;
    
    @JsonProperty("@type")
    private String type;
    private int version;
    private String name;
}