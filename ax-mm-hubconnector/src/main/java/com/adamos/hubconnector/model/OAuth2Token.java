package com.adamos.hubconnector.model;

import org.joda.time.DateTime;
import org.svenson.JSONProperty;
import org.svenson.converter.JSONConverter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2Token {
	
	@JsonProperty("access_token")
    private String accessToken;
    
	@JSONProperty("access_token")
    public String getAccessToken() {
    	return this.accessToken;
    }
    
	@JSONProperty("access_token")
    public void setAccessToken(String value) {
    	this.accessToken = value;
    }
    
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JSONProperty("token_type")
    public String getTokenType() {
    	return this.tokenType;
    }
    
    @JSONProperty("token_type")
    public void setTokenType(String value) {
    	this.tokenType = value;
    }

    @JsonProperty("expires_in")
    private long expiresIn;
    
    @JSONProperty("expires_in")
    public long getExpiresIn() {
    	return this.expiresIn;
    }
    
    @JSONProperty("expires_in")
    public void setExpiresIn(long value) {
    	this.expiresIn = value;
    }
    
    
    @JsonProperty("refresh_token")
    private long refreshToken;
    
    @JSONProperty("refresh_token")
    public long getRefreshToken() {
    	return this.refreshToken;
    }
    
    @JSONProperty("refresh_token")
    public void setRefreshToken(long value) {
    	this.refreshToken = value;
    }
    
    @JsonProperty("refresh_expires_in")
    private String refreshExpiresIn;
    
    @JSONProperty("refresh_expires_in")
    public String getRefreshExpiresIn() {
    	return this.refreshExpiresIn;
    }
    
    @JSONProperty("refresh_expires_in")
    public void setRefreshExpiresIn(String value) {
    	this.refreshExpiresIn = value;
    }
    
    @JsonProperty("expiry_date")
    private DateTime expiryDate;

    @JSONProperty("expiry_date")
    @JSONConverter(type=com.cumulocity.model.DateTimeConverter.class)
    public void setExpiryDate(DateTime value) {
    	this.expiryDate = value;
    }
    
    @JSONProperty("expiry_date")    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
	@JSONConverter(type=com.cumulocity.model.DateTimeConverter.class)
	public DateTime getExpiryDate() {
		return this.expiryDate;
	}
    
}