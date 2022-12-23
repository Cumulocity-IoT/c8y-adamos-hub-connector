package com.adamos.hubconnector.services;

import java.util.ArrayList;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import com.adamos.hubconnector.CustomProperties;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.events.EventMapping;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.tenant.OptionRepresentation;

/**
 * MigrationService
 */
@Service
public class MigrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);

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

    private void migrateToVersion_1_4_0() {
        LOGGER.info("Migration to version 1.4.0 started...");
        MigrationService.migrationRunning = true;

        ArrayList<OptionRepresentation> options = new ArrayList<>();
        OptionRepresentation optionEnvironment = new OptionRepresentation();
        optionEnvironment.setCategory(CustomProperties.HUB_GLOBAL_SETTINGS);
        optionEnvironment.setKey("environment");
        optionEnvironment.setValue("adamos-hub.dev");
        options.add(optionEnvironment);
        cumulocityService.updateTenantOptions(options);

        ManagedObjectRepresentation mo = cumulocityService
                .getManagedObjectByFragmentType(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE);
        if (mo != null) {
            mo.setProperty(CustomProperties.HUB_EVENTRULES_TO_HUB_OBJECT_TYPE, new EventMapping[0]);
            mo.setLastUpdatedDateTime(null);
            cumulocityService.updateManagedObject(mo);
        }
        

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