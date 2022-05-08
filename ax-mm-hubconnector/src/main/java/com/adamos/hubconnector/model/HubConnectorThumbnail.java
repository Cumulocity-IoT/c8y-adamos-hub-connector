package com.adamos.hubconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HubConnectorThumbnail {

	private String contentType;	// MIME-Type of the Data
	private String data;		// Base64-encoded Data of the image
	private String caption;
	private String title;
	
}
