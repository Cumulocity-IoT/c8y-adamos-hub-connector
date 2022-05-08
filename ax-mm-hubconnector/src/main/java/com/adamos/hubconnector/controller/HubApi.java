package com.adamos.hubconnector.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.adamos.hubconnector.model.HubConnectorResponse;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.adamos.hubconnector.model.hub.ManufacturerDTO;
import com.adamos.hubconnector.model.hub.TreeDTO;
import com.adamos.hubconnector.model.hub.hierarchy.TreeNode;
import com.adamos.hubconnector.services.AmqpService;
import com.adamos.hubconnector.services.CumulocityService;
import com.adamos.hubconnector.services.HubService;


@RestController
public class HubApi {
	
	@Autowired
	private HubService hubService;
	
	@Autowired
	private CumulocityService cumulocityService;
	
	@Autowired
	private AmqpService amqpService;
	
	private static final Logger logger = LoggerFactory.getLogger(HubApi.class);
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_READ')")
	@RequestMapping(value = "/manufacturerIdentities", method = RequestMethod.GET) 
	public ResponseEntity<List<ManufacturerDTO>> getManufacturerIdentities() {
		try {
			return new ResponseEntity<>(hubService.getManufacturerIdentities(), HttpStatus.OK);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error fetching manufacturer identities", hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error fetching manufacturer identities", rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	//@RequestMapping(value = "/hubTrees", method = RequestMethod.GET) 
	public ResponseEntity<List<TreeDTO>> getTree() {
		try {
			return new ResponseEntity<>(hubService.getTrees(), HttpStatus.OK);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error fetching tree", hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error fetching tree", rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_READ')")
	@RequestMapping(value = "/hierarchy/tree", method = RequestMethod.GET)
	public ResponseEntity<TreeNode<MDMObjectDTO>> getISA95Tree() {
		return new ResponseEntity<>(cumulocityService.getISA95Tree(), HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_READ')")
	@RequestMapping(value = "/assets", method = RequestMethod.GET) 
	public ResponseEntity<List<EquipmentDTO>> getAssets(@RequestParam("disconnected") Optional<Boolean> isDisconnected) {
		try {
			if (isDisconnected.orElse(false)) {
				return new ResponseEntity<>(hubService.getDisconnectedMachineTools(), HttpStatus.OK);
			}
			return new ResponseEntity<>(hubService.getMachineTools(), HttpStatus.OK);			
		} catch (HttpClientErrorException hcee) {
			logger.error("Error fetching equipment", hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error fetching equipment", rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_READ')")	
	@RequestMapping(value = "/assets/{id}", method = RequestMethod.GET)
	public ResponseEntity<HubConnectorResponse<EquipmentDTO>> getAsset(@PathVariable long id) {
		try {
			HubConnectorResponse<EquipmentDTO> obj = hubService.getAsset(id);
			if (obj != null) {
				return new ResponseEntity<HubConnectorResponse<EquipmentDTO>>(obj, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error fetching asset " + id, hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error fetching asset " + id, rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/disconnectAmqp", method = RequestMethod.GET)
	public ResponseEntity<?> disconnectAmqp() {
		this.amqpService.disconnectAmqpThread();
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/reconnectAmqp", method = RequestMethod.GET)
	public ResponseEntity<?> reconnectAmqp() {
		this.amqpService.restartAmqpSubscription();
		return new ResponseEntity<>(HttpStatus.OK);
	}	
		
}


