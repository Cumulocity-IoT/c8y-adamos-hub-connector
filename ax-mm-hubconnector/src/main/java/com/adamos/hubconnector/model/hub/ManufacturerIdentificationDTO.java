package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode
public class ManufacturerIdentificationDTO {
	private String buildYear;
	private String oemUniqueTypeIdentifier;
	private String serialNumber;
	private String internalName;
	private String manufacturingNumber;
	private String name;
	private OwnerDTO owner;
}

