package com.adamos.hubconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimpleSyncConfiguration {
	private boolean syncToHub;
	private boolean syncFromHub;
}