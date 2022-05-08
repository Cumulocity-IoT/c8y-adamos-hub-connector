package com.adamos.hubconnector.model.hub.hierarchy;

import com.adamos.hubconnector.model.hub.AddressDTO;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper = true)
public class SiteDTO extends MDMObjectDTO{
	private String name;
	private String timeZone;
	private WGS84CoordinatesDTO wgs84Coordinates;
	private AddressDTO address;
	@JsonProperty("@type")
	private String type;
	private int version;
}

