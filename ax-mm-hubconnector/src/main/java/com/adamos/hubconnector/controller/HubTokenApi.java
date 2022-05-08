package com.adamos.hubconnector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.adamos.hubconnector.model.OAuth2AppToken;
import com.adamos.hubconnector.services.HubService;

@RestController
public class HubTokenApi {

	@Autowired
	private HubService hubService;

	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
    @RequestMapping(value = "/token", method = RequestMethod.GET) 
	public OAuth2AppToken getToken() {
		return hubService.getOAuth2AppToken();		
	}
	
}
