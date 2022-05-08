package com.adamos.hubconnector.model;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import lombok.Data;

@Data
public class OAuth2AppToken {

	private long expiresIn;
	private String accessToken;
	private String tokenType;
	
	public OAuth2AppToken(OAuth2Token originalToken) {
		this.setExpiresIn(Seconds.secondsBetween(DateTime.now(), originalToken.getExpiryDate()).getSeconds());
		this.setTokenType(originalToken.getTokenType());
		this.setAccessToken(originalToken.getAccessToken());
	}
}
