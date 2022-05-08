package com.adamos.hubconnector.polling;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cumulocity.model.authentication.CumulocityBasicCredentials;
import com.cumulocity.model.authentication.CumulocityCredentials;
import com.cumulocity.sdk.client.PlatformParameters;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.notification.Subscriber;
import com.cumulocity.sdk.client.notification.SubscriberBuilder;
import com.cumulocity.sdk.client.notification.SubscriptionNameResolver;

@Configuration
public class ManagedObjectsSubscriberConfiguration {

	@Value("${C8Y.baseURL}")
    private String host;
	
	@Value("${C8Y.user}")
    private String username;
	
	@Value("${C8Y.tenant}")
    private String tenant;
	
	@Value("${C8Y.password}")
    private String password;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedObjectsSubscriberConfiguration.class);

	@Bean
    public Subscriber<Object, HashMap> subscriberApi() throws SDKException {
		LOGGER.info("SubscribingChanges managedObjects/* at " + host + " ...");

		// initialize credentials for channel subscription
		CumulocityCredentials creds = CumulocityBasicCredentials.builder().tenantId(tenant).username(username).password(password).build();
		PlatformParameters params = new PlatformParameters(host, creds, null);

		SubscriptionNameResolver<Object> resolver = new SubscriptionNameResolver<Object>() {
			@Override
			public String apply(Object arg0) {
				return arg0.toString();
			}
		};
		
		SubscriberBuilder<Object, HashMap> builder = SubscriberBuilder.anSubscriber();
		Subscriber<Object,HashMap> subscriber = builder.withDataType(HashMap.class)
													   .withParameters(params)
													   .withSubscriptionNameResolver(resolver)
													   .withRealtimeEndpoint().build();

		LOGGER.info("SubscriptionSuccessful subscription for " + host + " successful.");
		return subscriber;
    }
	
}
