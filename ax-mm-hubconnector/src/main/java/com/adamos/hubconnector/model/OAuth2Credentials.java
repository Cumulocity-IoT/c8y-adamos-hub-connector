package com.adamos.hubconnector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class OAuth2Credentials implements Cloneable {

	private String client_id;
	private String client_secret;
	private String grant_type;

	@Override
	public OAuth2Credentials clone() {
		OAuth2Credentials copy = new OAuth2Credentials();
		copy.setClient_id(this.client_id);
		copy.setClient_secret(this.client_secret);
		copy.setGrant_type(this.grant_type);
		return copy;
	}
}