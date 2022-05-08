package com.adamos.hubconnector.services;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.events.AdamosProcessingMode;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.adamos.hubconnector.model.hub.MDMObjectDTO;
import com.adamos.hubconnector.model.hub.hierarchy.AreaDTO;
import com.adamos.hubconnector.model.hub.hierarchy.LocationAssetDTO;
import com.adamos.hubconnector.model.hub.hierarchy.ProductionLineDTO;
import com.adamos.hubconnector.model.hub.hierarchy.SiteDTO;
import com.adamos.hubconnector.model.hub.hierarchy.TreeNode;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.model.ID;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.cumulocity.rest.representation.tenant.OptionRepresentation;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.identity.IdentityApi;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.cumulocity.sdk.client.inventory.InventoryFilter;
import com.cumulocity.sdk.client.option.TenantOptionApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.Lists;

@Service
public class CumulocityService {
	private static final Logger appLogger = LoggerFactory.getLogger(CumulocityService.class);
	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());
	private RestTemplate restTemplate;
	
	@Value("${C8Y.tenant}")
    private String tenant;	
	
	@Value("${C8Y.user}")
    private String c8yUser;

	@Value("${C8Y.password}")
    private String c8yPassword;
	
    @Value("${C8Y.baseURL}")
    private String c8yBaseUrl;
    
    @Autowired
    private MicroserviceSubscriptionsService service;
    
	@Autowired
	private InventoryApi inventoryApi;    
	
	@Autowired
	private IdentityApi identityApi;
	
	@Autowired
	private TenantOptionApi tenantOptionApi;
	
	
	public CumulocityService(RestTemplateBuilder restTemplateBuilder) {
		restTemplate = restTemplateBuilder.build();
	}

	@PostConstruct
	public void init() {
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(tenant + "/" + c8yUser, c8yPassword));
	}
	
	private HttpHeaders getHttpHeaders(AdamosProcessingMode processingMode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Cumulocity-Processing-Mode", processingMode.toString());
		return headers;
	}
	
	public MeasurementRepresentation createMeasurement(String json, AdamosProcessingMode processingMode) {
		service.callForTenant(tenant, () -> {
			try {
				HttpEntity<String> entity = new HttpEntity<>(json, getHttpHeaders(processingMode));
				return restTemplate.postForObject(c8yBaseUrl + "/measurement/measurements", entity, EventRepresentation.class);
			} catch (HttpClientErrorException|HttpServerErrorException ex) {
				appLogger.error(ex.getMessage(), ex);
				appLogger.error("Payload: " + json);	
				//throw ex;
				return null;
			}
		});
		
		return null;
	}
	
	public MeasurementRepresentation createMeasurement(MeasurementRepresentation measurement, AdamosProcessingMode processingMode) {
		return this.createMeasurement(measurement.toJSON(), processingMode);
	}
	
	public EventRepresentation createEvent(String json, AdamosProcessingMode processingMode) {
		service.callForTenant(tenant, () -> {
			try {
				HttpEntity<String> entity = new HttpEntity<>(json, getHttpHeaders(processingMode));
				return restTemplate.postForObject(c8yBaseUrl + "/event/events", entity, EventRepresentation.class);
			} catch (HttpClientErrorException|HttpServerErrorException ex) {
				appLogger.error(ex.getMessage(), ex);
				appLogger.error("Payload: " + json);	
				return null;
			}
		});
		
		return null;
	}
	
	public EventRepresentation createEvent(EventRepresentation event, AdamosProcessingMode processingMode) {
		return createEvent(event.toJSON(), processingMode);
	}
	
	public AlarmRepresentation createAlarm(String json, AdamosProcessingMode processingMode) {
		service.callForTenant(tenant, () -> {
			try {
				HttpEntity<String> entity = new HttpEntity<>(json, getHttpHeaders(processingMode));
				return restTemplate.postForObject(c8yBaseUrl + "/alarm/alarms", entity, AlarmRepresentation.class);
			} catch (HttpClientErrorException|HttpServerErrorException ex) {
				appLogger.error(ex.getMessage(), ex);
				appLogger.error("Payload: " + json);	
				return null;
			}
		});
		
		return null;
	}	

	public AlarmRepresentation createAlarm(AlarmRepresentation alarm, AdamosProcessingMode processingMode) {
		return this.createAlarm(alarm.toJSON(), processingMode);
	}	

	public List<ManagedObjectRepresentation> getManagedObjectsByFragmentType(String fragmentType) {
		return service.callForTenant(tenant, () -> {
		    InventoryFilter filter = new InventoryFilter();
		    filter.byFragmentType(fragmentType);
			Iterable<ManagedObjectRepresentation> objects = inventoryApi.getManagedObjectsByFilter(filter).get().allPages();
			if (objects != null) {
				return Lists.newArrayList(objects);
			}
			
			return null;
		});
	}

	public List<ManagedObjectRepresentation> getManagedObjectsByManagedObjectType(String managedObjectType) {
		return service.callForTenant(tenant, () -> {
			InventoryFilter filter = new InventoryFilter();
			filter.byType(managedObjectType);
			Iterable<ManagedObjectRepresentation> objects = inventoryApi.getManagedObjectsByFilter(filter).get().allPages();
			if (objects != null) {
				return Lists.newArrayList(objects);
			}
			
			return null;
		});
	}	

	public ExternalIDRepresentation setIdentity(long id, String typeName, String externalId) {
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

	public void deleteIdentity(String typeName, String externalId) {
		ExternalIDRepresentation exObj = new ExternalIDRepresentation();
		exObj.setType(typeName);
		exObj.setExternalId(externalId);
		
		try {
			identityApi.deleteExternalId(exObj);
		} catch (SDKException e) {
			// NotFound ignore
			if (e.getHttpStatus() != 404) {
				throw e;
			}
		}
	}
	
	public ManagedObjectRepresentation getManagedObjectByFragmentType(String fragmentType) {
		return service.callForTenant(tenant, () -> {
		    InventoryFilter filter = new InventoryFilter();
		    filter.byFragmentType(fragmentType);
			Iterable<ManagedObjectRepresentation> objects = inventoryApi.getManagedObjectsByFilter(filter).get().allPages();
			for (ManagedObjectRepresentation mo : objects) {
				return mo;
			}
			
			return null;
		});
	}
	
	public ManagedObjectRepresentation createManagedObject(String typeName, Object data) {
		return service.callForTenant(tenant, () -> {
			ManagedObjectRepresentation obj = new ManagedObjectRepresentation();
			obj.setType(typeName);
			obj.setProperty(typeName, data);
			return inventoryApi.create(obj);
		});
	}
	
	public ManagedObjectRepresentation updateManagedObject(ManagedObjectRepresentation obj) {
		return service.callForTenant(tenant, () -> {
			obj.setLastUpdatedDateTime(null);
			return inventoryApi.update(obj);
		});
	}
	
	public void createManagedObjectIfNotExists(String typeName, Object data) {
		if (this.getManagedObjectByFragmentType(typeName) == null) {
			createManagedObject(typeName, data);
			appLogger.info("Automated initial creation of " + typeName);
		}
	}
	
	public ManagedObjectRepresentation getManagedObjectByCustomId(String type, String extid) {
		return service.callForTenant(tenant, () -> {
	        ManagedObjectRepresentation managedObjectRepresentation = null;
			ExternalIDRepresentation externalId = getExternalIDRepresentationByCustomId(type, extid);
			
			if (externalId != null) {
				managedObjectRepresentation = externalId.getManagedObject();
				managedObjectRepresentation = inventoryApi.get(managedObjectRepresentation.getId());
			}
			
	        return managedObjectRepresentation;
		});
	}
	
	public ExternalIDRepresentation getExternalIDRepresentationByCustomId(String type, String extid) {
		return service.callForTenant(tenant, () -> {
	        ID id = new ID();
	        id.setType(type);
	        id.setValue(extid);
	        ExternalIDRepresentation externalId = null;
	        try {
	            externalId = identityApi.getExternalId(id);
	        } catch (SDKException exception) {
	            if (exception.getHttpStatus() != HttpStatus.NOT_FOUND.value()) {
	                throw exception;
	            }
	        }
	        
	        return externalId;
		});
	}	
	
	public boolean updateHubData(MDMObjectDTO object, String objectIdentityType) {
		ExternalIDRepresentation externalIDRepresentation = getExternalIDRepresentationByCustomId(objectIdentityType, object.getUuid());
		if (externalIDRepresentation != null) {
			return service.callForTenant(tenant, () -> {
				ManagedObjectRepresentation obj = externalIDRepresentation.getManagedObject();
				obj.setProperty(CustomProperties.HUB_DATA, object);
				obj.setLastUpdatedDateTime(null);
				inventoryApi.update(obj);

				return true;
			});
		}
		return false;
	}

	public boolean deleteHubObject(MDMObjectDTO object, String objectIdentityType) {
		ExternalIDRepresentation externalIDRepresentation = getExternalIDRepresentationByCustomId(objectIdentityType, object.getUuid());
		if (externalIDRepresentation != null) {
			return service.callForTenant(tenant, () -> {
				ManagedObjectRepresentation obj = externalIDRepresentation.getManagedObject();

				inventoryApi.delete(obj.getId());
				return true;
			});
		}
		return false;		
	}

	private <T extends LocationAssetDTO> TreeNode<MDMObjectDTO> addChildren(TreeNode<MDMObjectDTO> tree, List<ManagedObjectRepresentation> list, Class clazz) {
//	private <T extends MDMObjectDTO> TreeNode<MDMObjectDTO> addChildren(TreeNode<MDMObjectDTO> tree, List<ManagedObjectRepresentation> list, Class clazz) {	
		for (ManagedObjectRepresentation element : list) {
			T castedElement = (T)mapper.convertValue(element.getProperty(CustomProperties.HUB_DATA), clazz);

			TreeNode<MDMObjectDTO> findParent = tree.findTreeNode(new Comparable<MDMObjectDTO>(){
				@Override
				public int compareTo(MDMObjectDTO o) {
					if (castedElement.getLocationId() != null) {
						return o.getUuid().compareTo(castedElement.getLocationId());
					} else {
						return -1;
					}					
				}
			});

			if (findParent != null) findParent.addChild(castedElement);
		}

		return tree;
	}

	public TreeNode<MDMObjectDTO> getISA95Tree() {
		// Level 0 - Root-Element
		MDMObjectDTO root = new MDMObjectDTO();
		root.setUuid("");
		TreeNode<MDMObjectDTO> tree = new TreeNode<MDMObjectDTO>(root);

		// Level 1 - Sites
		List<ManagedObjectRepresentation> sites = this.getManagedObjectsByManagedObjectType(CustomProperties.Site.OBJECT_TYPE);
		for (ManagedObjectRepresentation site : sites) {
			tree.addChild(mapper.convertValue(site.getProperty(CustomProperties.HUB_DATA), SiteDTO.class));
		}

		// Level 2 - Areas
		List<ManagedObjectRepresentation> areas = this.getManagedObjectsByManagedObjectType(CustomProperties.Area.OBJECT_TYPE);
		tree = addChildren(tree, areas, AreaDTO.class);

		// Level 3 - Work centers
		List<ManagedObjectRepresentation> workCenters = this.getManagedObjectsByManagedObjectType(CustomProperties.ProductionLine.OBJECT_TYPE);
		tree = addChildren(tree, workCenters, ProductionLineDTO.class);

		// Level 4 - Equipment
		List<ManagedObjectRepresentation> devices = this.getManagedObjectsByFragmentType(CustomProperties.HUB_IS_DEVICE);
		tree = addChildren(tree, devices, EquipmentDTO.class);

		return tree;
	}

		
	public List<OptionRepresentation> getTenantOptions() {
		return service.callForTenant(tenant, () -> {
			return tenantOptionApi.getAllOptionsForCategory(CustomProperties.HUB_GLOBAL_SETTINGS);	
		});
	}
	
	public void updateTenantOptions(List<OptionRepresentation> options) {
		service.runForTenant(tenant, () -> {
			for (OptionRepresentation o : options) {
				tenantOptionApi.save(o);
			}
		});
	}
	
}