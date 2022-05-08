package com.adamos.hubconnector.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.HubProperties;
import com.adamos.hubconnector.model.MappingConfiguration;
import com.adamos.hubconnector.model.OAuth2AppToken;
import com.adamos.hubconnector.model.OAuth2Token;
import com.adamos.hubconnector.model.RestResponsePage;
import com.adamos.hubconnector.model.HubConnectorResponse;
import com.adamos.hubconnector.model.HubConnectorSettings;
import com.adamos.hubconnector.model.HubConnectorThumbnail;
import com.adamos.hubconnector.model.ImportStatistics;
import com.adamos.hubconnector.model.hub.CustomerIdentificationDTO;
import com.adamos.hubconnector.model.hub.ImageDTO;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.adamos.hubconnector.model.hub.ManufacturerIdentificationDTO;
import com.adamos.hubconnector.model.hub.ManufacturerDTO;
import com.adamos.hubconnector.model.hub.TreeDTO;
import com.adamos.hubconnector.model.hub.hierarchy.AreaDTO;
import com.adamos.hubconnector.model.hub.hierarchy.SiteDTO;
import com.adamos.hubconnector.model.hub.hierarchy.ProductionLineDTO;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.identity.IdentityApi;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Strings;

import c8y.Hardware;


@Service
public class HubService {
	private static final Logger LOGGER = LoggerFactory.getLogger(HubService.class);
	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());

	@Autowired
	private HubProperties appProperties;
	
	@Autowired
	InventoryApi inventoryApi;
	
	@Autowired
	IdentityApi identityApi;
	
	@Autowired
	private AuthTokenService authTokenService;
	
	@Autowired
	private HubConnectorService hubConnectorService;
	
    @Autowired
    private MicroserviceSubscriptionsService service;
	
	@Value("${C8Y.tenant}")
    private String tenant;
		
	private RestTemplate restTemplate;
	
	public HubService(RestTemplateBuilder restTemplateBuilder) {
		restTemplate = restTemplateBuilder.build();
	}
		
	public HubConnectorSettings getConnectorSettingsByObj(ManagedObjectRepresentation obj) {
		if (obj.hasProperty(CustomProperties.HUB_CONNECTOR_SETTINGS)) {
			return mapper.convertValue(obj.getProperty(CustomProperties.HUB_CONNECTOR_SETTINGS), HubConnectorSettings.class);
		}
		
		return null;
	}

	
	/**
	 * Creates an HubSettings-entry for an asset, if the asset exists, has the property c8y_IsDevice and
	 * if it doesn't have an HubSettings-entry.
	 * 
	 * @param id
	 * @return ManagedObjectRepresentation with new added HubSettings-property
	 */
	public ManagedObjectRepresentation connectDeviceToHub(long id) {
		return service.callForTenant(tenant, () -> {
			// Check if item is available
			ManagedObjectRepresentation device = inventoryApi.get(GId.asGId(id));
			
			// Only devices without HubSettings should be updated
			if (device.hasProperty(CustomProperties.C8Y.IS_DEVICE) && !device.hasProperty(CustomProperties.HUB_CONNECTOR_SETTINGS)) {
				createMachineTool(device);
				return inventoryApi.get(GId.asGId(id));
			}

			return null;
		});
	}
	
	public boolean disconnectDeviceFromHub(long id) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation item = inventoryApi.get(GId.asGId(id));

			if (item.hasProperty(CustomProperties.HUB_CONNECTOR_SETTINGS)) {
				
				HubConnectorSettings settings = mapper.convertValue(item.getProperty(CustomProperties.HUB_CONNECTOR_SETTINGS), HubConnectorSettings.class);

				item.setProperty(CustomProperties.HUB_CONNECTOR_SETTINGS, null);
				item.setProperty(CustomProperties.HUB_DATA, null);
				item.setProperty(CustomProperties.HUB_THUMBNAIL, null);
				item.setProperty(CustomProperties.HUB_IS_DEVICE, null);
				item.setLastUpdatedDateTime(null);
				
				inventoryApi.update(item);
				ExternalIDRepresentation exObj = new ExternalIDRepresentation();
				exObj.setType(CustomProperties.Machine.IDENTITY_TYPE);
				exObj.setExternalId(settings.getUuid());
				
				try {
					identityApi.deleteExternalId(exObj);
				} catch (SDKException e) {
					// NotFound ignore
					if (e.getHttpStatus() != 404) {
						throw e;
					}
				}
				
				return true;
			}
			return false;
		});
	}
	
	public ManagedObjectRepresentation updateHubDeviceSettings(long id, HubConnectorSettings settings) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation item = inventoryApi.get(GId.asGId(id));
			item.set(settings);
			item.setLastUpdatedDateTime(null);
			return inventoryApi.update(item);
		});
	}
	
	public ManagedObjectRepresentation duplicateMapping(long id) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation item = inventoryApi.get(GId.asGId(id));
			MappingConfiguration sourceMapping = item.get(MappingConfiguration.class);
			
			if (sourceMapping != null) {
				ManagedObjectRepresentation obj = new ManagedObjectRepresentation();
				
				MappingConfiguration destinationMapping = new MappingConfiguration(sourceMapping);
				obj.set(item.getName() + " (Copy)", "name");
				obj.set(destinationMapping);
				obj = inventoryApi.create(obj);
				
				return obj;
			}
			
			return null;
		});
	}
	
	public HubConnectorResponse<EquipmentDTO> getAsset(long id) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation item = inventoryApi.get(GId.asGId(id));
			
			if (item.hasProperty(CustomProperties.HUB_DATA)) {
				return new HubConnectorResponse<EquipmentDTO>(item, CustomProperties.HUB_DATA, EquipmentDTO.class);
			}
			
			return null;
		});
	}
	
	public boolean isDevice(long id) {
		return service.callForTenant(tenant, () -> {
			try {
				ManagedObjectRepresentation item = inventoryApi.get(GId.asGId(id));	
				return item != null && item.hasProperty(CustomProperties.C8Y.IS_DEVICE);			
			} catch(SDKException e) {
				if(e.getHttpStatus()==404) {
					return false;
				}
				throw e;
			}
		});
	}
	
	private <T,R> T restToHub(URI uri, HttpMethod method, R obj, Class<T> clazz) throws RestClientException {
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		HttpEntity<R> request = new HttpEntity<R>(obj, authTokenService.getHeaderBearerToken(org.springframework.http.MediaType.APPLICATION_JSON));
		T response =  restTemplate.exchange(uri, method, request, clazz).getBody();
		return response;
	}
	
	private boolean deleteInHub(URI uri) throws RestClientException {
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		HttpEntity<Void> request = new HttpEntity<Void>(authTokenService.getHeaderBearerToken(org.springframework.http.MediaType.APPLICATION_JSON));
		ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class);
		
		return (response.getStatusCode() == HttpStatus.OK);
	}
	
	private EquipmentDTO updateEquipment(EquipmentDTO device) {
		URI uriPut = UriComponentsBuilder.fromUriString(appProperties.getAdamosMdmServiceEndpoint())
										 .path("asset/machines/" + device.getUuid())
										 .build().toUri();
		
		return restToHub(uriPut, HttpMethod.PUT, device, EquipmentDTO.class);
	}
	
	
	public ImportStatistics importHierarchy() {
		int importedPlants = 0;
		int importedAreas = 0;
		int importedWorkcenters = 0;
	
		LOGGER.info("Importing plants");
        for (SiteDTO plant : getPlants()) {
            importHubPlant(plant);
            importedPlants++;
        }
        LOGGER.info("Imported {} plants", importedPlants);
		LOGGER.info("Importing areas");
        for (AreaDTO area : getAreas()) {
            importHubArea(area);
            importedAreas++;
        }
        LOGGER.info("Imported {} areas", importedAreas);
		LOGGER.info("Importing work centers");
        for (ProductionLineDTO workCenter : getProductionLines()) {
            importProductionLine(workCenter);
            importedWorkcenters++;
        }
        LOGGER.info("Imported {} work centers", importedWorkcenters);
        
        ImportStatistics statistics = new ImportStatistics();
        statistics.setImportedPlants(importedPlants);
        statistics.setImportedAreas(importedAreas);
        statistics.setImportedWorkcenters(importedWorkcenters);
        return statistics;
	}
	
	public EquipmentDTO createMachineTool(ManagedObjectRepresentation obj) {
		EquipmentDTO device = new EquipmentDTO();
		device.setEquipmentType(CustomProperties.Machine.EQUIPMENT_MACHINETOOL);
		
		String name = obj.getName().isBlank() ? "Unknown " + obj.getId().getValue() : obj.getName();
		String serialNumber = "0";
		String model = "Unknown Model";
		if (obj.hasProperty(CustomProperties.C8Y.HARDWARE)) {
			Hardware c8yHardware = mapper.convertValue(obj.getProperty(CustomProperties.C8Y.HARDWARE), Hardware.class);
			if (!Strings.isNullOrEmpty(c8yHardware.getSerialNumber())) {
				serialNumber = c8yHardware.getSerialNumber();
			}
			if (!Strings.isNullOrEmpty(c8yHardware.getModel())) {
				model = c8yHardware.getModel();
			}
		}
		
		ManufacturerIdentificationDTO mi = new ManufacturerIdentificationDTO();
		mi.setName(model);
		mi.setSerialNumber(serialNumber);
		device.setManufacturerIdentification(mi);

		CustomerIdentificationDTO ci = new CustomerIdentificationDTO();
		ci.setName(name);
		device.setCustomerIdentification(ci);
		
		URI uriService = UriComponentsBuilder.fromUriString(appProperties.getAdamosMdmServiceEndpoint()).path("asset/machines").build().toUri();
		device = restToHub(uriService, HttpMethod.POST, device, EquipmentDTO.class);
		
		setIdentity(obj.getId().getLong(), CustomProperties.Machine.IDENTITY_TYPE, device.getUuid());
		obj.setProperty(CustomProperties.HUB_DATA, device);
		obj.setProperty(CustomProperties.HUB_CONNECTOR_SETTINGS, hubConnectorService.initConnectorSettings(device.getUuid()));
		obj.setLastUpdatedDateTime(null);
		inventoryApi.update(obj);
		
		return device;
	}
	
	private ExternalIDRepresentation setIdentity(long id, String typeName, String externalId) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation obj = new ManagedObjectRepresentation(); // inventoryApi.get(GId.asGId(id));
			obj.setId(GId.asGId(id));
			ExternalIDRepresentation externalIdObj = new ExternalIDRepresentation();
			externalIdObj.setExternalId(externalId);
			externalIdObj.setType(typeName);
			externalIdObj.setManagedObject(obj);
			
			return identityApi.create(externalIdObj);
		});
	}
	
	public OAuth2AppToken getOAuth2AppToken() {
		OAuth2Token currentToken = authTokenService.getToken();
		
		return new OAuth2AppToken(currentToken);
	}	
	
	private URI getUrlString(String serviceUri, String path, MultiValueMap<String, String> params) {
		return UriComponentsBuilder.fromHttpUrl(serviceUri)
				   .path(path)
				   .queryParams(params)
				   .build().encode().toUri();
	}

	private <T> Page<T> getHubPageResponse(String serviceUri, String token, String path, MultiValueMap<String, String> params, ParameterizedTypeReference<Page<T>> paramTypeRef) throws RestClientException {
		URI requestUrl = getUrlString(serviceUri, path, params);

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", "Bearer " + token);
		HttpEntity<Page<T>> requestEntity = new HttpEntity<Page<T>>(null, requestHeaders);
		ResponseEntity<Page<T>> response = restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, paramTypeRef);
		
		
		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		}			
		
		return null;
	}
	
	private <T> T getHubResponse(String serviceUri, String token, String path, MultiValueMap<String, String> params, ParameterizedTypeReference<T> paramTypeRef) throws RestClientException {
		URI requestUrl = getUrlString(serviceUri, path, params);

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", "Bearer " + token);
		HttpEntity<T> requestEntity = new HttpEntity<T>(null, requestHeaders);
		ResponseEntity<T> response = restTemplate.exchange(
				requestUrl,
			    HttpMethod.GET,
			    requestEntity,
			    paramTypeRef);
		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		}			

		return null;
	}	
	
	public boolean checkAndUpdateDevice(String uuid, boolean isFromHub, boolean isForceImageUpdate) {
		return service.callForTenant(tenant, () -> {
			boolean isChangeDetected = false;
			
			ManagedObjectRepresentation device = hubConnectorService.getDeviceByHubUuid(uuid);
			if (device != null) {
				HubConnectorSettings settings = getConnectorSettingsByObj(device);
				
				boolean isUpdateActive = isFromHub ? settings.getSyncConfiguration().isSyncFromHub() : settings.getSyncConfiguration().isSyncToHub();
				
				if (isHubDevice(device) && isUpdateActive) {
					isChangeDetected = checkAndUpdateDevice(device, isFromHub, isForceImageUpdate);
				}
			} 
			
			return isChangeDetected;		
		});
	}	
	
	public boolean deleteDeviceInC8Y(String uuid) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation device = hubConnectorService.getDeviceByHubUuid(uuid);
			if (device != null) {
				inventoryApi.delete(device.getId());
				return true;
			} 
			
			return false;		
		});		
	}
	
	public boolean deleteDeviceInHub(String uuid) {
			URI uriDeleteDevice = UriComponentsBuilder.fromUriString(appProperties.getAdamosMdmServiceEndpoint())
					 .path("asset/machines/" + uuid)
					 .build().toUri();
			
			return deleteInHub(uriDeleteDevice);
	}
	
	public boolean checkAndUpdateDevice(String uuid) {
		return service.callForTenant(tenant, () -> {
			boolean isChangeDetected = false;
			
			ManagedObjectRepresentation device = hubConnectorService.getDeviceByHubUuid(uuid);
			if (device != null) {
				HubConnectorSettings settings = getConnectorSettingsByObj(device);
				
				if (isHubDevice(device) && settings.getSyncConfiguration().isSyncFromHub()) {
					isChangeDetected = checkAndUpdateDevice(device, true, false);
				}
			} 
			
			return isChangeDetected;		
		});
	}
	
	private boolean isHubDevice(ManagedObjectRepresentation device) {
		return device != null && device.hasProperty(CustomProperties.HUB_CONNECTOR_SETTINGS) && device.hasProperty(CustomProperties.HUB_DATA);		
	}
	
	public boolean checkAndUpdateDevice(ManagedObjectRepresentation device, boolean isFromHub, boolean isForceImageUpdate) {
		
		return service.callForTenant(tenant, () -> {
			boolean isChangeDetected = false;
			
			if (isHubDevice(device)) {
				
				HubConnectorSettings settings = getConnectorSettingsByObj(device);

				try {
					// Read current Data in Hub
					EquipmentDTO hubDevice = getMachineTool(settings.getUuid());

					if (hubDevice != null) {
						EquipmentDTO currentDeviceData = mapper.convertValue(device.getProperty(CustomProperties.HUB_DATA), EquipmentDTO.class);
						
						// Name or comment changed?
						if (!hubDevice.getCustomerIdentification().getName().equals(device.getName()) || 
							(hubDevice.getCustomerIdentification().getComment() != null && !hubDevice.getCustomerIdentification().getComment().equals(currentDeviceData.getCustomerIdentification().getComment()))) {
							isChangeDetected = true;
							if (isFromHub) {
								device.setName(hubDevice.getCustomerIdentification().getName());
							} else {
								hubDevice.getCustomerIdentification().setName(device.getName());
								hubDevice.getCustomerIdentification().setComment(currentDeviceData.getCustomerIdentification().getComment());							
								hubDevice = updateEquipment(hubDevice);
							}
						}
						
						// Serial number changed?
						if (device.hasProperty(CustomProperties.C8Y.HARDWARE)) {
							Hardware c8yHardware = mapper.convertValue(device.getProperty(CustomProperties.C8Y.HARDWARE), Hardware.class);
							if (!Strings.isNullOrEmpty(c8yHardware.getSerialNumber()) && 
								!c8yHardware.getSerialNumber().equals(hubDevice.getManufacturerIdentification().getSerialNumber())) {
								isChangeDetected = true;
								if (isFromHub) {
									c8yHardware.setSerialNumber(hubDevice.getManufacturerIdentification().getSerialNumber());
									device.setProperty(CustomProperties.C8Y.HARDWARE, c8yHardware);
								} else {
									hubDevice.getManufacturerIdentification().setSerialNumber(c8yHardware.getSerialNumber());
									hubDevice = updateEquipment(hubDevice);
								}
							}
						}
		
						// OemUniqueTypeIdentifier changed? -> new Thumbnail-Image has to be added
						if (isFromHub && ((hubDevice.getManufacturerIdentification().getOemUniqueTypeIdentifier() != null && !hubDevice.getManufacturerIdentification().getOemUniqueTypeIdentifier().equals(currentDeviceData.getManufacturerIdentification().getOemUniqueTypeIdentifier())) || isForceImageUpdate)) {
							setTumbnailByOemId(device, hubDevice.getManufacturerIdentification().getOemUniqueTypeIdentifier());
							isChangeDetected = true;
						}
									
						if (isChangeDetected) {
							// Save data to Inventory
							device.setProperty(CustomProperties.HUB_DATA, hubDevice);
							settings.setLastSync(DateTime.now().withZone(DateTimeZone.UTC));
							device.setProperty(CustomProperties.HUB_CONNECTOR_SETTINGS, settings);
							device.setLastUpdatedDateTime(null);
							inventoryApi.update(device);
							LOGGER.info("deviceUpdated " + device.getId().getValue());
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Error while updateing device", ex);
					throw ex;
				}
				
			}
			
			return isChangeDetected;
		});
	}
	
	private String getBase64EncodedImage(String imageURL) throws IOException {
	    java.net.URL url = new java.net.URL(imageURL); 
	    InputStream is = url.openStream();  
	    byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(is); 
	    return Base64.encodeBase64String(bytes);
	}

	
	
	public List<EquipmentDTO> getDisconnectedMachineTools() {
		List<EquipmentDTO> allMachineTools = getMachineTools();
		List<EquipmentDTO> disconnectedMachineTools = new ArrayList<EquipmentDTO>();
		
		for (EquipmentDTO machineTool : allMachineTools) {
			if (hubConnectorService.getExternalIdByHubUuid(machineTool.getUuid()) == null) {
				disconnectedMachineTools.add(machineTool);
			}
		}
		
		return disconnectedMachineTools;
	}

	public ManagedObjectRepresentation importHubPlant(SiteDTO plant) {
		return importHubData(plant, CustomProperties.Site.OBJECT_TYPE, CustomProperties.Site.IDENTITY_TYPE);
	}

	public ManagedObjectRepresentation importHubPlant(String uuid) {
		SiteDTO plant = getPlant(uuid);
		if (plant != null) {
			return importHubPlant(plant);
		}

		return null;
	}

	public ManagedObjectRepresentation importHubArea(AreaDTO area) {
		return importHubData(area, CustomProperties.Area.OBJECT_TYPE, CustomProperties.Area.IDENTITY_TYPE);
	}

	public ManagedObjectRepresentation importHubArea(String uuid) {
		AreaDTO area = getArea(uuid);
		if (area != null) {
			return importHubArea(area);
		}

		return null;
	}

	public ManagedObjectRepresentation importProductionLine(ProductionLineDTO productionLine) {
		return importHubData(productionLine, CustomProperties.ProductionLine.OBJECT_TYPE, CustomProperties.ProductionLine.IDENTITY_TYPE);
	}

	public ManagedObjectRepresentation importProductionLine(String uuid) {
		ProductionLineDTO productionLine = getProductionLine(uuid);
		if (productionLine != null) {
			return importProductionLine(productionLine);
		}

		return null;
	}	

	private ManagedObjectRepresentation importHubData(MDMObjectDTO object, String objectType, String objectIdentityType) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation result = new ManagedObjectRepresentation();
			if (!Strings.isNullOrEmpty(objectType)) {
				result.setType(objectType);
			}
			result.setProperty(CustomProperties.HUB_DATA, object);
			result = inventoryApi.create(result);
					
			if (!Strings.isNullOrEmpty(objectIdentityType)) {
				setIdentity(result.getId().getLong(), objectIdentityType, object.getUuid());		
			}

			return result;
		});		
	}

	public ManagedObjectRepresentation importHubDevice(String uuid, boolean isDevice) {
		return service.callForTenant(tenant, () -> {
			EquipmentDTO source = getMachineTool(uuid);
			ManagedObjectRepresentation target = null;
			
			try {
				if (hubConnectorService.getExternalIdByHubUuid(uuid) == null) {
					target = new ManagedObjectRepresentation();

					target.setName(source.getCustomerIdentification().getName());

					if(source.getManufacturerIdentification() != null) {
						if (!Strings.isNullOrEmpty(source.getManufacturerIdentification().getSerialNumber())) {
							Hardware c8yHardware = new Hardware();
							c8yHardware.setSerialNumber(source.getManufacturerIdentification().getSerialNumber());
							target.setProperty(CustomProperties.C8Y.HARDWARE, c8yHardware);
						}

						if (!Strings.isNullOrEmpty(source.getManufacturerIdentification().getOemUniqueTypeIdentifier())) {
							setTumbnailByOemId(target, source.getManufacturerIdentification().getOemUniqueTypeIdentifier());
						}
					}
					
					if (isDevice) {
						target.setProperty(CustomProperties.C8Y.IS_DEVICE, new Object());
					}
					target.setProperty(CustomProperties.HUB_IS_DEVICE, true);
					target.setProperty(CustomProperties.HUB_DATA, source);
					target.setProperty(CustomProperties.HUB_CONNECTOR_SETTINGS, hubConnectorService.initConnectorSettings(source.getUuid()));
					target = inventoryApi.create(target);
					
					// set externalId in Cumulocity
					setIdentity(target.getId().getLong(), CustomProperties.Machine.IDENTITY_TYPE, source.getUuid());
				}	
			} catch (java.lang.NullPointerException ex) {
				LOGGER.error("Error while importing ADAMOS-Hub device id = " + uuid, ex);
				return target;
			}

			
			return target;
		});
	}
	
	private void setTumbnailByOemId(ManagedObjectRepresentation target, String oemId) {
		try {
			if (!Strings.isNullOrEmpty(oemId)) {
				List<ImageDTO> images = getThumbnailsByOemId(oemId);
				if (images != null && !images.isEmpty()) {
					HubConnectorThumbnail image = new HubConnectorThumbnail();
					image.setCaption(images.get(0).getCaption());
					image.setTitle(images.get(0).getTitle());
	//				image.setContentType(images.get(0).getContentType());
					try {
						String urlImage = images.get(0).getContentUrl();
						image.setContentType(getContentTypeByUrl(urlImage));
						image.setData(getBase64EncodedImage(urlImage));
					} catch (IOException e) {
						e.printStackTrace();
					}
					target.setProperty(CustomProperties.HUB_THUMBNAIL, image);
					return;
				}
			}
			
			// Delete the current thumbnail, if no data was found
			target.setProperty(CustomProperties.HUB_THUMBNAIL, null);
		} catch (Exception ex) {
			LOGGER.error("Error while setting thumbnail for DeviceId = " + target.getId().toString(), ex);
		}
	}
	
	private String getContentTypeByUrl(String urlName) throws IOException {
		URL url = new URL(urlName);
		HttpURLConnection connection = (HttpURLConnection)  url.openConnection();
		connection.setRequestMethod("HEAD");
		connection.connect();
		return connection.getContentType();
	}
	
	public List<EquipmentDTO> getMachineTools() {
		return getListOfPages("asset/machines", new TypeReference<List<EquipmentDTO>>() { });
	}
	
	public List<TreeDTO> getTrees() {
		return getListOfPages("hierarchy/trees", new TypeReference<List<TreeDTO>>() { });
	}
	
	public EquipmentDTO getMachineTool(String uuid) {
		return getHubResponse(appProperties.getAdamosMdmServiceEndpoint(), authTokenService.getToken().getAccessToken(), "asset/machines/" + uuid, new LinkedMultiValueMap<>(), new ParameterizedTypeReference<EquipmentDTO>() {});
	}

	public SiteDTO getPlant(String uuid) {
		return getHubResponse(appProperties.getAdamosMdmServiceEndpoint(), authTokenService.getToken().getAccessToken(), "asset/sites/" + uuid, new LinkedMultiValueMap<>(), new ParameterizedTypeReference<SiteDTO>() {});
	}

	public AreaDTO getArea(String uuid) {
		return getHubResponse(appProperties.getAdamosMdmServiceEndpoint(), authTokenService.getToken().getAccessToken(), "asset/areas/" + uuid, new LinkedMultiValueMap<>(), new ParameterizedTypeReference<AreaDTO>() {});
	}

	public ProductionLineDTO getProductionLine(String uuid) {
		return getHubResponse(appProperties.getAdamosMdmServiceEndpoint(), authTokenService.getToken().getAccessToken(), "asset/workCenters/productionLines/" + uuid, new LinkedMultiValueMap<>(), new ParameterizedTypeReference<ProductionLineDTO>() {});
	}

	public <T> RestResponsePage<T> getHubResponsePage(String path, int page, int size) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("page", Integer.toString(page));
		params.add("size", Integer.toString(size));
		RestResponsePage<T> result = getHubResponse(appProperties.getAdamosMdmServiceEndpoint(), authTokenService.getToken().getAccessToken(), path, params, new ParameterizedTypeReference<RestResponsePage<T>>() {}); 
		return result;
	}

	public <T> List<T> getListOfPages(String path, TypeReference<List<T>> typeReference) {
		List<T> response = new ArrayList<T>();
		RestResponsePage<T> page = getHubResponsePage(path, 0, 100);
		if (page != null && page.hasContent()) {
			response.addAll(mapper.convertValue(page.getContent(), typeReference));
			while (page.hasNext()) {
				page = getHubResponsePage(path, page.getNumber() + 1 , 100);
				if (page.hasContent()) {
					response.addAll(mapper.convertValue(page.getContent(), typeReference));
				}
			}
		}

		return response;
	}

	public List<SiteDTO> getPlants() {
		return getListOfPages("asset/sites", new TypeReference<List<SiteDTO>>() { });
	}

	public List<AreaDTO> getAreas() {
		return getListOfPages("asset/areas", new TypeReference<List<AreaDTO>>() { });
	}

	public List<ProductionLineDTO> getProductionLines() {
		return getListOfPages("asset/workCenters/productionLines", new TypeReference<List<ProductionLineDTO>>() { });
	}
	
	public EquipmentDTO updateMachineTool(EquipmentDTO data) {
		URI uriAddManufacturer = UriComponentsBuilder.fromUriString(appProperties.getAdamosMdmServiceEndpoint())
				 .path("asset/machines/" + data.getUuid())
				 .build().toUri();

		return restToHub(uriAddManufacturer, HttpMethod.PUT, data, EquipmentDTO.class);
	}
	
	public List<ImageDTO> getThumbnailsByOemId(String oemId) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("imageSize", "THUMBNAIL");
//		params.add("lang", "en");		
		return getHubResponse(appProperties.getAdamosCatalogServiceEndpoint(), authTokenService.getToken().getAccessToken(), "catalog/equipmentOem/" + oemId + "/productImages", params, new ParameterizedTypeReference<List<ImageDTO>>() {}); 
	}
	
	public List<ManufacturerDTO> getManufacturerIdentities() {
		return getListOfPages("asset/manufacturers", new TypeReference<List<ManufacturerDTO>>() {});
	}
	
}

