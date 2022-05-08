package com.adamos.hubconnector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper=false)
public class GlobalSyncPlatformConfigurationExtended extends GlobalSyncPlatformConfiguration {
	private boolean c8yIsDevice = true;
}
