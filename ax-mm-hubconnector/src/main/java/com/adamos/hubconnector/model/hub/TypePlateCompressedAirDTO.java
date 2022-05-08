package com.adamos.hubconnector.model.hub;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class TypePlateCompressedAirDTO {
	private double pressureMax;
	private double pressureMin;
	private double pressureNormal;
	private double volumeFlowNominal;
}
