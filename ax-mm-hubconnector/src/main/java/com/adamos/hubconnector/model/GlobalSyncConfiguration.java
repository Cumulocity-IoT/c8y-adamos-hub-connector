package com.adamos.hubconnector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalSyncConfiguration {
	private GlobalSyncPlatformConfigurationExtended hubToAdamos;
	private GlobalSyncPlatformConfiguration adamosToHub;
	
	public GlobalSyncConfiguration() {
		this.hubToAdamos = new GlobalSyncPlatformConfigurationExtended();
		this.adamosToHub = new GlobalSyncPlatformConfiguration();
	}
}
