package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode
public class ManufacturerDTO extends MDMObjectDTO {
	private String adamosEcosystemId;
	private String adamosManufacturerId;
	private String name;
	private String uniqueManufacturerPrefix;
	private AddressDTO address;
	private int version;
}

