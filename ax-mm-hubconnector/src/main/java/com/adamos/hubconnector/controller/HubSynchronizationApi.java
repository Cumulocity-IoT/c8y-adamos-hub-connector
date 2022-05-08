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

import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.ImportStatistics;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.adamos.hubconnector.services.HubConnectorService;
import com.adamos.hubconnector.services.HubService;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Strings;

@RestController
public class HubSynchronizationApi {

	@Autowired
	private HubService hubService;
	
	@Autowired
	private HubConnectorService hubConnectorService;
	
	private static final Logger logger = LoggerFactory.getLogger(HubSynchronizationApi.class);
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/synchronization/toHub/{id}", method = RequestMethod.POST)
	public ResponseEntity<ManagedObjectRepresentation> connectDeviceToHub(@PathVariable long id) {
		try {
			ManagedObjectRepresentation obj = hubService.connectDeviceToHub(id);
			if (obj != null) {
				return new ResponseEntity<ManagedObjectRepresentation>(obj, HttpStatus.CREATED);
			}
			
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error connecting asset " + id, hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error connecting asset " + id, rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/synchronization/unlink/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> deleteHubSettings(@PathVariable long id) {
		try {
			if (hubService.disconnectDeviceFromHub(id)) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error deleting asset " + id, hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error deleting asset " + id, rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/synchronization/fromHub/{uuid}", method = RequestMethod.POST)
	public ResponseEntity<ManagedObjectRepresentation> importHubDevice(@PathVariable String uuid, @RequestParam(defaultValue="", name="isDevice") String isC8yDevice) {
		try {
			boolean isDevice = false;
			if (Strings.isNullOrEmpty(isC8yDevice)) {
				HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();
				isDevice = globalSettings.getDefaultSyncConfiguration().getHubToAdamos().isC8yIsDevice();
			} else {
				isDevice = Boolean.parseBoolean(isC8yDevice);
			}
			ManagedObjectRepresentation obj = hubService.importHubDevice(uuid, isDevice);
			if (obj != null) {
				return new ResponseEntity<ManagedObjectRepresentation>(obj, HttpStatus.CREATED);
			}
			
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error importing device " + uuid, hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error importing device " + uuid, rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/synchronization/fromHub", method = RequestMethod.POST)
	public ResponseEntity<ImportStatistics> importAllHubDevices(@RequestParam(name="isDevice") Optional<Boolean> isC8yDevice,
			@RequestParam Optional<Boolean> includeHierarchy) {
		try {	
			boolean isDevice = isC8yDevice.orElseGet(() -> {
				HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();
				return globalSettings.getDefaultSyncConfiguration().getHubToAdamos().isC8yIsDevice();
			});
			boolean importHierarchy = includeHierarchy.orElse(false);
			
			ImportStatistics statistics = new ImportStatistics();
			
			if(importHierarchy) {
				statistics = hubService.importHierarchy();
			}
			
			List<EquipmentDTO> devices = hubService.getDisconnectedMachineTools();
			logger.info("Importing devices");
			for (EquipmentDTO device : devices) {
				hubService.importHubDevice(device.getUuid(), isDevice);	
			}
			logger.info("Imported {} devices", devices.size());
			statistics.setImportedDevices(devices.size());
			return new ResponseEntity<ImportStatistics>(HttpStatus.CREATED);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error importing devices", hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error importing devices", rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADAMOS_HUB_ADMIN')")
	@RequestMapping(value = "/synchronization/resync/{uuid}", method = RequestMethod.POST)
	public ResponseEntity<ManagedObjectRepresentation> resyncFromHub(@PathVariable String uuid, @RequestParam(value = "source", defaultValue = "hub") String source) {
		try {	
			if (hubService.checkAndUpdateDevice(uuid, source.equals("hub"), true)) {
				ManagedObjectRepresentation device = hubConnectorService.getDeviceByHubUuid(uuid);
				return new ResponseEntity<ManagedObjectRepresentation>(device, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (HttpClientErrorException hcee) {
			logger.error("Error re-syncing device " + uuid, hcee);
			return new ResponseEntity<>(hcee.getStatusCode());
		} catch (RestClientException rce) {
			logger.error("Error re-syncing device " + uuid, rce);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
