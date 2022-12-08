package com.adamos.hubconnector.model;

import org.svenson.JSONProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class HubConnectorGlobalSettings {

	private OAuth2Credentials oAuth2Credentials;

	@JsonProperty("oAuth2Credentials")
	@JSONProperty("oAuth2Credentials")
	public void setOAuth2Credentials(OAuth2Credentials value) {
		this.oAuth2Credentials = value;
	}

	@JsonProperty("oAuth2Credentials")
	@JSONProperty("oAuth2Credentials")
	public OAuth2Credentials getOAuth2Credentials() {
		return this.oAuth2Credentials;
	}

	private OAuth2Credentials amqpCredentials;

	@JsonProperty("amqpCredentials")
	@JSONProperty("amqpCredentials")
	public void setAmqpCredentials(OAuth2Credentials value) {
		this.amqpCredentials = value;
	}

	@JsonProperty("amqpCredentials")
	@JSONProperty("amqpCredentials")
	public OAuth2Credentials getAmqpCredentials() {
		return this.amqpCredentials;
	}

	@JsonProperty("defaultSyncConfiguration")
	private GlobalSyncConfiguration defaultSyncConfiguration;

	private Environment environment;

	@JsonProperty("environment")
	@JSONProperty("environment")
	public void setEnvironment(Environment value) {
		this.environment = value;
	}

	@JsonProperty("environment")
	@JSONProperty("environment")
	public Environment getEnvironment() {
		return this.environment;
	}

	private String version = null;

	public HubConnectorGlobalSettings(boolean init) {
		if (init) {
			defaultSyncConfiguration = new GlobalSyncConfiguration();
			defaultSyncConfiguration.getAdamosToHub().setCreate(false);
			defaultSyncConfiguration.getAdamosToHub().setUpdate(false);
			defaultSyncConfiguration.getAdamosToHub().setDelete(false);
			defaultSyncConfiguration.getHubToAdamos().setCreate(false);
			defaultSyncConfiguration.getHubToAdamos().setUpdate(false);
			defaultSyncConfiguration.getHubToAdamos().setDelete(false);
			oAuth2Credentials = new OAuth2Credentials();
			environment = new Environment();
		}
	}

	public String getOAuthEndpoint() {
		return getEnvironment().getUrl() + "/auth-service/token";
	}

	public String getAdamosMdmServiceEndpoint() {
		return getEnvironment().getUrl() + "/mdm-service/v1/";
	}

	public String getAdamosCatalogServiceEndpoint() {
		return getEnvironment().getUrl() + "/catalog-service/v1/";
	}

	public String getAdamosEventServiceEndpoint() {
		return getEnvironment().getUrl() + "/event-service/v1/";
	}

	public String getAdamosAmqpEndpoint() {
		return "amqps://messaging." + getEnvironment().getUrl();
	}
}
