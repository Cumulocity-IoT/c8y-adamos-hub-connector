package com.adamos.hubconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GlobalSyncPlatformConfiguration {
	private boolean create = false;
	private boolean update = false;
	private boolean delete = false;
}
