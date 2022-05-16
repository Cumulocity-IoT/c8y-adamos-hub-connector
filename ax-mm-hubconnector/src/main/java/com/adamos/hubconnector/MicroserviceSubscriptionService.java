package com.adamos.hubconnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.adamos.hubconnector.services.AmqpService;
import com.adamos.hubconnector.services.AuthTokenService;
import com.adamos.hubconnector.services.EventRulesService;
import com.adamos.hubconnector.services.HubConnectorService;
import com.adamos.hubconnector.services.MigrationService;
import com.cumulocity.microservice.subscription.model.MicroserviceSubscriptionAddedEvent;

/**
 * Events to be triggered, after service is subscribed
 */
@Service
public class MicroserviceSubscriptionService {

	private final Logger appLogger = LoggerFactory.getLogger(MicroserviceSubscriptionService.class);
	
	@Autowired
	AuthTokenService tokenService;
	
	@Autowired
	HubConnectorService hubConnectorService;
	
	@Autowired
	EventRulesService eventService;

	@Autowired
	MigrationService migrationService;
	
	@Autowired
	AmqpService amqpService;
	
	@EventListener
	public void onAdded(MicroserviceSubscriptionAddedEvent event) {
	    appLogger.info("subscriberAdded Subscription added for tenant: " + event.getCredentials().getTenant());
	   
	    try {
		    if (hubConnectorService.getGlobalSettings() == null) {
		    	hubConnectorService.initGlobalSettings();
			    eventService.initMappingRules();
		    }  else {
		    	if(tokenService.getToken() != null) {
					migrationService.checkMigrations();
					amqpService.restartAmqpSubscription();		    		
		    	}
		    }	    	
	    } catch (Throwable t) {
	    	appLogger.error("Caught error in microservice subscription handler", t);
	    }
	}
		
}
