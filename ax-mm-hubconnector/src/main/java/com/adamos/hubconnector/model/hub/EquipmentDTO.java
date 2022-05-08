package com.adamos.hubconnector.model.hub;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper = true)
public class EquipmentDTO extends MDMObjectDTO {

	public EquipmentDTO(boolean createSubclasses) {
		if (createSubclasses) {
			this.components = new ArrayList<String>();
			this.customerIdentification = new CustomerIdentificationDTO();
			this.locationIdentification = new LocationIdentificationDTO();
			this.manufacturerIdentification = new ManufacturerIdentificationDTO();
			this.systemSoftware = new SystemSoftwareDTO();
			this.owner = new OwnerDTO();
			this.typePlate = new TypePlateDTO();
		}		
	}

	@JsonProperty("@type")
	private String type;
	private int version;
	private String manufacturerId;
	private String manufacturerName;
	private List<String> components;
	private CustomerIdentificationDTO customerIdentification;
	private String equipmentType;
	private LocationIdentificationDTO locationIdentification;
	private ManufacturerIdentificationDTO manufacturerIdentification;
	private String modelVersion;
	private SystemSoftwareDTO systemSoftware;
	private TypePlateDTO typePlate;
	
}