package com.adamos.hubconnector.model.hub;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
//@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode
public class SystemSoftwareDTO {
	private List<SoftwareDTO> assignedPackages;
	private SoftwareDTO mainSoftware;
	
	public SystemSoftwareDTO() {
		assignedPackages = new ArrayList<SoftwareDTO>();
	}
}
