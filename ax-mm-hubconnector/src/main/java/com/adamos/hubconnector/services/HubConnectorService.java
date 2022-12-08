package com.adamos.hubconnector.services;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.Environment;
import com.adamos.hubconnector.model.GlobalSyncConfiguration;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.HubConnectorSettings;
import com.adamos.hubconnector.model.OAuth2Credentials;
import com.adamos.hubconnector.model.SimpleSyncConfiguration;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.tenant.OptionRepresentation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@Service
public class HubConnectorService {
	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());
	private static final Logger appLogger = LoggerFactory.getLogger(HubConnectorService.class);
	
	@Autowired
	private CumulocityService cumulocityService;
		
	@Autowired
	private AuthTokenService authTokenService;
	
	@Autowired
	private AmqpService amqpService;	

	public HubConnectorGlobalSettings saveGlobalSettings(HubConnectorGlobalSettings globalSettings) {
		return this.saveGlobalSettings(globalSettings, false);
	}

	public HubConnectorGlobalSettings getGlobalSettings() {
		HubConnectorGlobalSettings settings = new HubConnectorGlobalSettings();
		List<OptionRepresentation> options = cumulocityService.getTenantOptions();
		if(options.isEmpty()) {
			return null;
		}
		for (OptionRepresentation o : options) {
			try {
				switch(o.getKey()) {
				case "oAuth2Credentials":
					settings.setOAuth2Credentials(mapper.readValue(o.getValue(), OAuth2Credentials.class));
					break;
				case "amqpCredentials":
					settings.setAmqpCredentials(mapper.readValue(o.getValue(), OAuth2Credentials.class));
					break;
				case "defaultSyncConfiguration":
					settings.setDefaultSyncConfiguration(mapper.readValue(o.getValue(), GlobalSyncConfiguration.class));			
					break;
				case "environment":
					settings.setEnvironment(mapper.readValue(o.getValue(), Environment.class));
					break;
				case "version":
					settings.setVersion(o.getValue());
					break;
				default:
					break;
				}				
			} catch(Exception e) {
				appLogger.error("Error reading global options", e);
			}
		}
		return settings;
	}


	
	public HubConnectorGlobalSettings saveGlobalSettings(HubConnectorGlobalSettings globalSettings, boolean isVersionChangeAllowed) {
		boolean IsCredentialUpdate = false;
		HubConnectorGlobalSettings oldSettings = getGlobalSettings();
		if(oldSettings != null) {
			if (!isVersionChangeAllowed) {
				// Always ignore changes in the version-field - this field should only be changed in the migration-process
				globalSettings.setVersion(oldSettings.getVersion());
			}
			
			if (oldSettings.getAmqpCredentials() != null) { 
				if (oldSettings.getAmqpCredentials().getClient_secret() == null) oldSettings.getAmqpCredentials().setClient_secret("");
				if (oldSettings.getAmqpCredentials().getClient_id() == null) oldSettings.getAmqpCredentials().setClient_id("");
				
				if (!oldSettings.getAmqpCredentials().getClient_secret().equals(globalSettings.getAmqpCredentials().getClient_secret()) ||
				    !oldSettings.getAmqpCredentials().getClient_id().equals(globalSettings.getAmqpCredentials().getClient_id())) {
						IsCredentialUpdate = true;
				}
			}
						
			if (IsCredentialUpdate) {
				authTokenService.setCurrentToken(null);
			}				
		}
		
		try {
			List<OptionRepresentation> options = new ArrayList<OptionRepresentation>();
			OptionRepresentation optionOAuth2Credentials = new OptionRepresentation();
			optionOAuth2Credentials.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
			optionOAuth2Credentials.setKey("credentials.oAuth2Credentials");
			optionOAuth2Credentials.setValue(mapper.writeValueAsString(globalSettings.getOAuth2Credentials()));
			options.add(optionOAuth2Credentials);

			OptionRepresentation optionAmqpCredentials = new OptionRepresentation();
			optionAmqpCredentials.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
			optionAmqpCredentials.setKey("credentials.amqpCredentials");
			optionAmqpCredentials.setValue(mapper.writeValueAsString(globalSettings.getAmqpCredentials()));
			options.add(optionAmqpCredentials);

			
			OptionRepresentation optionDefaultSyncConfiguration = new OptionRepresentation();
			optionDefaultSyncConfiguration.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
			optionDefaultSyncConfiguration.setKey("defaultSyncConfiguration");
			optionDefaultSyncConfiguration.setValue(mapper.writeValueAsString(globalSettings.getDefaultSyncConfiguration()));
			options.add(optionDefaultSyncConfiguration);

			OptionRepresentation optionEnvironment = new OptionRepresentation();
			optionEnvironment.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
			optionEnvironment.setKey("environment");
			optionEnvironment.setValue(mapper.writeValueAsString(globalSettings.getEnvironment()));
			options.add(optionEnvironment);

			OptionRepresentation optionVersion = new OptionRepresentation();
			optionVersion.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
			optionVersion.setKey("version");
			optionVersion.setValue(globalSettings.getVersion());
			options.add(optionVersion);

			cumulocityService.updateTenantOptions(options);		
			
		} catch (JsonProcessingException jpe) {
			appLogger.error("Error storing tenant options: ", jpe);
		}
		
		if (IsCredentialUpdate) {
			amqpService.restartAmqpSubscription();
		}
		
		return globalSettings;
	}	
	
	public void initGlobalSettings() {
		HubConnectorGlobalSettings globalSettings = new HubConnectorGlobalSettings(true);
		saveGlobalSettings(globalSettings);
	}	
	
	public HubConnectorSettings initConnectorSettings(String uuid) {
		HubConnectorGlobalSettings globalSettings = getGlobalSettings();
		HubConnectorSettings settings = new HubConnectorSettings();
		settings.setInitialSync(DateTime.now().withZone(DateTimeZone.UTC));
		settings.setLastSync(settings.getInitialSync());
		settings.setSyncConfiguration(new SimpleSyncConfiguration());
		settings.getSyncConfiguration().setSyncFromHub(globalSettings.getDefaultSyncConfiguration().getHubToAdamos().isUpdate());
		settings.getSyncConfiguration().setSyncToHub(globalSettings.getDefaultSyncConfiguration().getAdamosToHub().isUpdate());
		settings.setUuid(uuid);
		
		return settings;
	}	
	
	public ManagedObjectRepresentation getDeviceByHubUuid(String uuid) {
		ManagedObjectRepresentation managedObjectRepresentation = cumulocityService.getManagedObjectByCustomId(CustomProperties.Machine.IDENTITY_TYPE, uuid);
		if (managedObjectRepresentation == null) {
			appLogger.warn("Could not find HubDevice with uuid = " + uuid);
		}
		
		return managedObjectRepresentation;
	}
	
	public ExternalIDRepresentation getExternalIdByHubUuid(String uuid) {
		return cumulocityService.getExternalIDRepresentationByCustomId(CustomProperties.Machine.IDENTITY_TYPE, uuid);
	}
}
