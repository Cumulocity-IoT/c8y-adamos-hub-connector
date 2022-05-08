package com.adamos.hubconnector.model.hub.hierarchy;

import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
//@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper = true)
public class LocationAssetDTO extends MDMObjectDTO {
    private String name;
    private String locationId;    
}