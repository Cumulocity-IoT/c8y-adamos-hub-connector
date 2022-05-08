package com.adamos.hubconnector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Current custom App-Settings for HubConnector
 */
@Component
@ConfigurationProperties("app.hubconnector")
@Data
public class HubProperties {
	private String replaceHost(String address) {
		return address.replace("{{host}}", this.getHost());
	}
	
	private String oAuthEndpoint;
	public void setOAuthEndpoint(String value) {
		this.oAuthEndpoint = value;
	}
	public String getOAuthEndpoint() {
		return this.getHttpSchema() + replaceHost(this.oAuthEndpoint);
	}
	
	private String httpSchema;
	private String host;
	
	private String adamosMdmServiceEndpoint;
	public void setAdamosMdmServiceEndpoint(String value) {
		this.adamosMdmServiceEndpoint = value;
	}
	public String getAdamosMdmServiceEndpoint() {
		return this.getHttpSchema() + replaceHost(this.adamosMdmServiceEndpoint) + "/";
	}
	
	private String adamosCatalogServiceEndpoint;
	public void setAdamosCatalogServiceEndpoint(String value) {
		this.adamosCatalogServiceEndpoint = value;
	}
	public String getAdamosCatalogServiceEndpoint() {
		return this.getHttpSchema() + replaceHost(this.adamosCatalogServiceEndpoint) + "/";
	}	
	
	private String adamosAmqpEndpoint;
	public void setAdamosAmqpEndpoint(String value) {
		this.adamosAmqpEndpoint = value;
	}
	public String getAdamosAmqpEndpoint() {
		return replaceHost(this.adamosAmqpEndpoint);
	}	
}
