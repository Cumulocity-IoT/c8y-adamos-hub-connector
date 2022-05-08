package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class TypePlateDTO {
	private TypePlateCompressedAirDTO compressedAir;
	private TypePlateElectricalDTO electrical;
	private TypePlateMechanicalDTO mechanical;
}
