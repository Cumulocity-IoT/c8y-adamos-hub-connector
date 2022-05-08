package com.adamos.hubconnector.amqp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamos.hubconnector.model.hub.AmqpMessageDTO;
import com.adamos.hubconnector.services.EventRulesService;
import com.adamos.hubconnector.services.HubStandardEventsService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class AmqpConsumer extends DefaultConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConsumer.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private EventRulesService eventService;
	private HubStandardEventsService hubStandardEventsService;
	
    public AmqpConsumer(Channel channel, EventRulesService eventService, HubStandardEventsService hubStandardEventsService) {
		super(channel);
		this.eventService = eventService;
		this.hubStandardEventsService = hubStandardEventsService;
		mapper.registerModule(new JodaModule());
	}

	@Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body)
        throws IOException
    {
        
        String contentType = properties.getContentType();
        String messageBody = new String(body);
        LOGGER.info(messageBody);

        if (contentType != null && contentType.toLowerCase().trim().equals("application/json")) {
        	try {
	        	AmqpMessageDTO message = mapper.readValue(messageBody, AmqpMessageDTO.class);
	        	DocumentContext jsonContext = JsonPath.parse(messageBody);
	        	
	        	if (message.getReferenceObjectType() != null) {
					try {
						this.hubStandardEventsService.handleEvent(message);
						this.eventService.consumeHubMessage(messageBody, jsonContext);
					} catch (Exception ex) {
						LOGGER.error("Could not consume ADAMOS-Hub-Event AMQP-Message: " + messageBody, ex);
					}
				} else {
					try {
						this.eventService.consumeHubMessage(messageBody, jsonContext);
					} catch (Exception ex) {
						LOGGER.error("Could not consume ADAMOS-Hub-Event AMQP-Message: " + messageBody, ex);
					}
				}
        	} catch (JsonParseException|JsonMappingException ex) {
        		LOGGER.error("Could not typecast ADAMOS-Hub-Event AMQP-Message: " + messageBody, ex);
        	} catch (Exception ex2) {
        		LOGGER.error("Could not typecast ADAMOS-Hub-Event AMQP-Message: " + messageBody, ex2);
        	}

        } else {
        	LOGGER.info("Unknown ADAMOS-Hub-Event AMQP-Message: " + messageBody);
        }
    }
 }
