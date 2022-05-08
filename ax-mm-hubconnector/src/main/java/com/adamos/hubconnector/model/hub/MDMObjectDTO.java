package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode
public class MDMObjectDTO {
    protected String uuid;
    protected OwnerDTO owner;
    protected int versionId = 1;
}