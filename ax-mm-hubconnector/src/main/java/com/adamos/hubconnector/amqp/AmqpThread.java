package com.adamos.hubconnector.amqp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.adamos.hubconnector.HubProperties;
import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.services.EventRulesService;
import com.adamos.hubconnector.services.HubConnectorService;
import com.adamos.hubconnector.services.HubStandardEventsService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

@Component
public class AmqpThread implements Runnable, HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpThread.class);
    
    private CachingConnectionFactory connectionFactory;
   
	@Autowired
	private HubConnectorService hubConnectorService;

	@Autowired
	private HubStandardEventsService hubStandardEventsService;
	
    @Autowired
    private EventRulesService eventService;
    
	@Autowired
	private HubProperties appProperties;
	
	private boolean isConnected = false;
	
	private int countConnectionRetry = 0;

		
    @Override
    public void run() {
    	this.isConnected = connect();
    }
    
    private Channel channel;
    
    @Bean
    public Channel getChannel() {
    	return channel;
    }
    
    private Connection connection;
    private DefaultConsumer consumer;
    
    public boolean connect() {
        		
        try {
        	this.isConnected = false;
        	
        	final HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();
        	
            final String username = "sag-hubconnector-app-82e6b954-fc30-4d81-8574-7777061b7c17";//;globalSettings.getOAuth2Credentials().getClient_id();
            final String password = "67a651c3-196d-44c2-8e83-fa0f3b9c4aad";//globalSettings.getOAuth2Credentials().getClient_secret()
            //final String channelName = "sag-hubconnector-app";//globalSettings.getOAuth2Credentials().getClient_id();
            connectionFactory = new CachingConnectionFactory(new URI(appProperties.getAdamosAmqpEndpoint()+"/sag-hubconnector-app-82e6b954-fc30-4d81-8574-7777061b7c17"));
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setRequestedHeartBeat(60);
            
            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("Host: " + connectionFactory.getHost());
            	LOGGER.debug("User: " + connectionFactory.getUsername());
            	LOGGER.debug("Password: " + password);
            }

        	countConnectionRetry = 0;
       		int multiplier = 1;
        	
        	while (connection == null || !connection.isOpen()) {
	        	try {
	                LOGGER.info("Opening AMQP-Connection...");
	            	connection = connectionFactory.getRabbitConnectionFactory().newConnection();
	        	} catch (final com.rabbitmq.client.AuthenticationFailureException ex) {
	        		LOGGER.error("AMQP-connection to \"" + appProperties.getAdamosAmqpEndpoint() + "\" failed...");
	        		LOGGER.error("Authentication failure - please fix your credentials and retry");
	        		LOGGER.error(ex.getMessage());
	        		throw ex;
	        	} catch (final java.net.ConnectException ex) {
	        		LOGGER.error("AMQP-connection to \"" + appProperties.getAdamosAmqpEndpoint() + "\" failed...");
	        		LOGGER.error(ex.getMessage());
	        		try {
						TimeUnit.SECONDS.sleep(5 * multiplier);
					} catch (final InterruptedException e) {
						countConnectionRetry++;
						// Slowdown retry by increasing the multiplier every 100 retries 
						// maximum slowdown should be capped with 5 minute (60 * 50 = 300 sec = 5 min) intervals
						if (countConnectionRetry % 100 == 1 && multiplier < 60) {
							multiplier++;
						}
						
					}
	        	}
        	}
        	
            LOGGER.info("AMQP-connection to \"" + connection.getAddress().getHostName() + "\" established...");
            
            channel = connection.createChannel();

            channel.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(final ShutdownSignalException cause) {
                    if (cause.isInitiatedByApplication()) {
                    	LOGGER.info("AMQP-connection shutdown by application.");
                    } else {
                    	LOGGER.error("AMQP-connection shutdown NOT initiated by application.");
                        LOGGER.error(cause.getMessage());
                        reconnect();
                    }
                }
            });            
            
            consumer = new AmqpConsumer(channel, eventService, hubStandardEventsService);
        	channel.basicConsume("inbound", true, consumer);
        	
            LOGGER.info("Waiting for AMQP-messages on \"" + username + "\"...");
            
            this.isConnected = true;
            
            return true;
            	
    	} catch (final com.rabbitmq.client.ShutdownSignalException e) {
    		LOGGER.error(e.getMessage(), e);
    		return false;
    	} catch (final com.rabbitmq.client.AuthenticationFailureException e) {
    		return false;
        } catch (final IOException e) {
        	final Throwable cause = e.getCause();
        	if (cause != null && cause.getClass().getName().endsWith("ShutdownSignalException")) {
        		LOGGER.error(e.getCause().getMessage(), e);
        	} else {
        		LOGGER.error(e.getMessage(), e);
        	}
        	return false;
		} catch (final TimeoutException e) {
    		LOGGER.error(e.getMessage(), e);
    		return false;
		} catch (final URISyntaxException e) {
    		LOGGER.error(e.getMessage(), e);
			return false;
		}
    }
    
    public void disconnect() throws IOException {
		if (connection != null && connection.isOpen()) {
			try {
				connection.close();
				this.isConnected = false;
			} catch (final IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw e;
			}
		}
    }
    
    public boolean reconnect() {
    	if (connection != null) {
        	LOGGER.info("Closing AMQP-connection to \"" + connection.getAddress().getHostName() + "\"...");
    	}
    	try {
//    		if (connection != null && connection.isOpen()) {
//    			this.isConnected = false;
//    			connection.close();
//    		}
    		this.disconnect();
			
			return connect();
		} catch (final IOException e) {
	    	return false;
		}
    }

	@Override
	public Health health() {
		if (this.isConnected) {
			return Health.up().build();
		} 
		
		return Health.down().build();
	}
    
    
}
