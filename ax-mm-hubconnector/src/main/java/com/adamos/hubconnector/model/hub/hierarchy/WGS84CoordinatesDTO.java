package com.adamos.hubconnector.model.hub.hierarchy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class WGS84CoordinatesDTO {
	private float latitude;
	private float longitude;
}
