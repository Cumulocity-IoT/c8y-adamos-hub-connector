package com.adamos.hubconnector.polling;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cumulocity.sdk.client.notification.Subscriber;
import com.cumulocity.sdk.client.notification.Subscription;
import com.cumulocity.sdk.client.notification.SubscriptionListener;

@Component
public class ManagedObjectsSubscriber implements CommandLineRunner {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedObjectsSubscriber.class);

	@Autowired
	Subscriber<Object, HashMap> subscriber;
	
	@Autowired
	ManagedObjectsHandlerService subscriptionHander;
	
    @Override
    public void run(String...args) throws Exception {
    	
		subscriber.subscribe("/managedobjects/*", new SubscriptionListener<Object, HashMap>() {

			@Override
			public void onError(Subscription<Object> arg0, Throwable arg1) {
				LOGGER.info("Exception " + arg1.getMessage());
			}

			@Override
			public void onNotification(Subscription<Object> arg0, HashMap arg1) {
				subscriptionHander.handleSubscriptionData(arg1);
			}
			
		});
		
    }
}
