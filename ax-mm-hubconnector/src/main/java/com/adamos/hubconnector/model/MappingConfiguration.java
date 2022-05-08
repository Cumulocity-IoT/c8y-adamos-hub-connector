package com.adamos.hubconnector.model;

import com.adamos.hubconnector.model.hub.EquipmentDTO;

import lombok.Data;

@Data
public class MappingConfiguration {
	private String version = "0.0.1";

	private EquipmentDTO model;

	public MappingConfiguration() {
		model = new EquipmentDTO(true);
	}
	
	public MappingConfiguration(MappingConfiguration existingConfiguration) {
		this();
		this.version = existingConfiguration.getVersion();
		this.model = existingConfiguration.getModel();	
	}
	
}
