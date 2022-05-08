package com.adamos.hubconnector.model;

import org.joda.time.DateTime;
import org.svenson.converter.JSONConverter;

import lombok.Data;
import lombok.Setter;

@Data
public class HubConnectorSettings {
	
	private SimpleSyncConfiguration syncConfiguration;
	
	@Setter
	private DateTime lastSync;

	@JSONConverter(type=com.cumulocity.model.DateTimeConverter.class)
	public DateTime getLastSync() {
		return this.lastSync;
	}
	
	@Setter
	private DateTime initialSync;
	
	@JSONConverter(type=com.cumulocity.model.DateTimeConverter.class)
	public DateTime getInitialSync() {
		return this.initialSync;
	}
	
	private String mappingId;
	
	private String uuid;
	
	public HubConnectorSettings() {
		syncConfiguration = new SimpleSyncConfiguration();
	}

}
