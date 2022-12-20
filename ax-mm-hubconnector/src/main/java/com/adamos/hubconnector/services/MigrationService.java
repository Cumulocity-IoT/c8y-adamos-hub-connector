package com.adamos.hubconnector.services;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.hub.EquipmentDTO;
import com.adamos.hubconnector.model.hub.hierarchy.AreaDTO;
import com.adamos.hubconnector.model.hub.hierarchy.SiteDTO;
import com.adamos.hubconnector.model.hub.hierarchy.ProductionLineDTO;
import com.adamos.hubconnector.model.migration.ConnectorSettingsRenameToHub;
import com.adamos.hubconnector.model.migration.EventRulesRenameToHub;
import com.adamos.hubconnector.model.migration.GlobalSettingsRenameToHub;
import com.adamos.hubconnector.model.migration.IAdditionalObjectChanges;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.tenant.OptionRepresentation;
import com.cumulocity.sdk.client.SDKException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

/**
 * MigrationService
 */
@Service
public class MigrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());

    private static boolean migrationRunning = false;

    @Autowired
    CumulocityService cumulocityService;

    @Autowired
    HubConnectorService hubConnectorService;

    @Autowired
    HubService hubService;

    @Autowired
    BuildProperties buildProperties;

    public static boolean isMigrationRunning() {
        return MigrationService.migrationRunning;
    }

    private void migrateFragmentForEachManagedObject(final String sourceFragment, final String destinationFragment,
            boolean setType) {
        this.migrateFragmentForEachManagedObject(sourceFragment, destinationFragment, setType, null);
    }

    private void migrateFragmentForEachManagedObject(final String sourceFragment, final String destinationFragment,
            boolean setType, final IAdditionalObjectChanges additionalObjectChanges) {
        final List<ManagedObjectRepresentation> list = cumulocityService
                .getManagedObjectsByFragmentType(sourceFragment);

        for (ManagedObjectRepresentation obj : list) {
            // Copy old property to new property
            obj.setProperty(destinationFragment, obj.getProperty(sourceFragment));

            // Later we will delete the source-fragment
            obj.setProperty(sourceFragment, null);

            // If additional changes are defined invoke them here
            if (additionalObjectChanges != null) {
                obj = additionalObjectChanges.applyChanges(destinationFragment, obj);
            }

            // Delete DateTime - else the SDK crashes while updating
            obj.setLastUpdatedDateTime(null);

            if (setType) {
                // Set the type of the managed object equal to the destinationFragment-name
                obj.setType(destinationFragment);
            }

            // Update the object in C8Y
            cumulocityService.updateManagedObject(obj);
        }
    }

    private void migrateIdentityOfDevices() {
        final List<ManagedObjectRepresentation> list = cumulocityService
                .getManagedObjectsByFragmentType("adamos_hub_data");

        for (final ManagedObjectRepresentation obj : list) {
            String hub_uuid = ((HashMap<String, Object>) obj.getProperty("adamos_hub_data")).get("uuid").toString();
            try {
                cumulocityService.setIdentity(obj.getId().getLong(), "adamos_hub_machineTool_uuid", hub_uuid);
                cumulocityService.deleteIdentity("adamos_xhub_machineTool_uuid", hub_uuid);
            } catch (SDKException e) {
                LOGGER.error("Error migrating identity for device " + hub_uuid + ": " + e.getMessage());
            }
        }
    }

    private DefaultArtifactVersion getCurrentVersionFromJar() {
        String version = buildProperties.getVersion();
        if (version.contains("-")) {
            version = version.split("-")[0];
        }

        return new DefaultArtifactVersion(version);
    }

    private DefaultArtifactVersion getCurrentVersionFromGlobalSettings(HubConnectorGlobalSettings globalSettings) {
        String version = "0.0.0";
        if (globalSettings.getVersion() != null) {
            version = globalSettings.getVersion();
        }

        return new DefaultArtifactVersion(version);
    }

    private void migrateToVersion_0_1_0() {
        LOGGER.info("Migration to version 0.1.0 started...");
        MigrationService.migrationRunning = true;
        this.migrateFragmentForEachManagedObject("adamos_xhub_globalSettings", "adamos_hub_globalSettings", true,
                new GlobalSettingsRenameToHub());

        this.migrateFragmentForEachManagedObject("adamos_xhub_eventRules_FromXHub", "adamos_hub_eventRules_FromHub",
                true, new EventRulesRenameToHub(true));// Type
        this.migrateFragmentForEachManagedObject("adamos_xhub_eventRules_ToXHub", "adamos_hub_eventRules_ToHub", true,
                new EventRulesRenameToHub(false));// Type

        this.migrateFragmentForEachManagedObject("adamos_xhub_data", "adamos_hub_data", false);
        this.migrateFragmentForEachManagedObject("adamos_xhub_connectorSettings", "adamos_hub_connectorSettings", false,
                new ConnectorSettingsRenameToHub());
        this.migrateFragmentForEachManagedObject("adamos_xhub_thumbnail", "adamos_hub_thumbnail", false);

        this.migrateIdentityOfDevices();
        MigrationService.migrationRunning = false;
        LOGGER.info("Migration to version 0.1.0 finished...");
    }

    private void migrateToVersion_0_1_1() {
        LOGGER.info("Migration to version 0.1.1 started...");
        MigrationService.migrationRunning = true;
        final List<ManagedObjectRepresentation> list = cumulocityService
                .getManagedObjectsByFragmentType("adamos_hub_data");

        for (ManagedObjectRepresentation obj : list) {
            String hub_uuid = ((HashMap<String, Object>) obj.getProperty("adamos_hub_data")).get("uuid").toString();
            EquipmentDTO equipment = hubService.getMachineTool(hub_uuid);

            if (equipment != null) {
                LOGGER.info("Updating data for device '{}'.", hub_uuid);
                obj.setProperty(CustomProperties.HUB_DATA, equipment);
                obj.setProperty(CustomProperties.HUB_IS_DEVICE, true);
                obj.setLastUpdatedDateTime(null);
                cumulocityService.updateManagedObject(obj);
            } else {
                LOGGER.warn("Could not find device '{}' in hub - removing data from C8Y.", hub_uuid);
                hubService.disconnectDeviceFromHub(obj.getId().getLong());
            }
        }
        for (SiteDTO plant : hubService.getPlants()) {
            hubService.importHubPlant(plant);
        }
        for (AreaDTO area : hubService.getAreas()) {
            hubService.importHubArea(area);
        }
        for (ProductionLineDTO productionLine : hubService.getProductionLines()) {
            hubService.importProductionLine(productionLine);
        }
        MigrationService.migrationRunning = false;
        LOGGER.info("Migration to version 0.1.1 finished...");
    }

    private void migrateToVersion_1_4_0() {
        LOGGER.info("Migration to version 1.4.0 started...");
        MigrationService.migrationRunning = true;

        ArrayList<OptionRepresentation> options = new ArrayList<>();
        OptionRepresentation optionEnvironment = new OptionRepresentation();
        optionEnvironment.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
        optionEnvironment.setKey("environment");
        optionEnvironment.setValue("https://adamos-hub.com");
        options.add(optionEnvironment);
        cumulocityService.updateTenantOptions(options);

        MigrationService.migrationRunning = false;
        LOGGER.info("Migration to version 1.4.0 finished...");
    }

    public void checkMigrations() {
        LOGGER.info("Checking if migration is required");
        HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();
        ManagedObjectRepresentation oldGlobalSettings = cumulocityService
                .getManagedObjectByFragmentType("adamos_xhub_globalSettings");

        DefaultArtifactVersion jarVersion = getCurrentVersionFromJar();
        DefaultArtifactVersion globalVersion = null;
        if (oldGlobalSettings != null) {
            globalVersion = new DefaultArtifactVersion("0.0.0");
        } else {
            globalVersion = getCurrentVersionFromGlobalSettings(globalSettings);
        }
        DefaultArtifactVersion currentVersion = globalVersion;

        if (!currentVersion.equals(jarVersion)) {
            LOGGER.info("Migration is required");

            if (currentVersion.compareTo(new DefaultArtifactVersion("0.1.0")) < 0) {
                migrateToVersion_0_1_0();
                currentVersion = new DefaultArtifactVersion("0.1.0");
                globalSettings.setVersion(currentVersion.toString());
                hubConnectorService.saveGlobalSettings(globalSettings, true);
            }

            if (currentVersion.compareTo(new DefaultArtifactVersion("0.1.1")) < 0) {
                migrateToVersion_0_1_1();
                currentVersion = new DefaultArtifactVersion("0.1.1");
                globalSettings.setVersion(currentVersion.toString());
                hubConnectorService.saveGlobalSettings(globalSettings, true);
            }

            if (currentVersion.compareTo(new DefaultArtifactVersion("1.4.0")) < 0) {
                migrateToVersion_1_4_0();
                currentVersion = new DefaultArtifactVersion("1.4.0");
                globalSettings.setVersion(currentVersion.toString());
                hubConnectorService.saveGlobalSettings(globalSettings, true);
            }

            globalSettings.setVersion(jarVersion.toString());
            hubConnectorService.saveGlobalSettings(globalSettings, true);
        } else {
            LOGGER.info("No migration is required");
        }

    }

}