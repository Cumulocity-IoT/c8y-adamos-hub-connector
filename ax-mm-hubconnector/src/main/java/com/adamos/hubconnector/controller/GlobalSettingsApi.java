package com.adamos.hubconnector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.services.HubConnectorService;

@RestController
public class GlobalSettingsApi {

	@Autowired
	private HubConnectorService hubConnectorService;
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/globalSettings", method = RequestMethod.GET)
	public ResponseEntity<HubConnectorGlobalSettings> getGlobalSettings() {
		HubConnectorGlobalSettings obj = hubConnectorService.getGlobalSettings();
		if (obj != null) {
			return new ResponseEntity<HubConnectorGlobalSettings>(obj, HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/globalSettings", method = RequestMethod.PUT)
	public ResponseEntity<HubConnectorGlobalSettings> createHubSettings(@RequestBody HubConnectorGlobalSettings globalSettings) {
		HubConnectorGlobalSettings obj = hubConnectorService.saveGlobalSettings(globalSettings);
		if (obj != null) {
			return new ResponseEntity<HubConnectorGlobalSettings>(obj, HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}	
}
