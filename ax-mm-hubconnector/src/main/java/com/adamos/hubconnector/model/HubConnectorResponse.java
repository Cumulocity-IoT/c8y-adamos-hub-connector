package com.adamos.hubconnector.model;

import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HubConnectorResponse<T> {
	private String id;
	private String name;
	private T data;
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public HubConnectorResponse(ManagedObjectRepresentation obj, Class<T> clazz) {
		this.setId(obj.getId().getValue());
		this.setName(obj.getName());
		
		if (obj.hasProperty(clazz.getName().replace(".","_"))) {
			this.setData(obj.get(clazz));
		}
	}
	
	public HubConnectorResponse(ManagedObjectRepresentation obj, String propertyName, Class<T> clazz) {
		this.setId(obj.getId().getValue());
		this.setName(obj.getName());
		
		if (obj.hasProperty(propertyName)) {
			this.setData(mapper.convertValue(obj.getProperty(propertyName), clazz));
		}
	}	
}
