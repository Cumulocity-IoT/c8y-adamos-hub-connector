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
public class CustomerIdentificationDTO {
	private String comment;
	private String inventoryNumber;
	private String name;
	private String supervisorName;
}
