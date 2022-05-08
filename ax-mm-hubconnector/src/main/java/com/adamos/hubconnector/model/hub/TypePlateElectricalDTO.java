package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class TypePlateElectricalDTO {
	private double currentMax;
	private double currentNominal;
	private double frequencyNominal;
	private double powerMax;
	private double powerNominal;
	private double voltageNominal;
}
