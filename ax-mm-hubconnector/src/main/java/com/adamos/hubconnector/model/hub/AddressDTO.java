package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class AddressDTO {
	private String city;
	private String country;
	private String postalCode;
	private String region;
	private String street;
}
