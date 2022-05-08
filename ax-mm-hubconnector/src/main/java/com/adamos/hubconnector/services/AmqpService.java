package com.adamos.hubconnector.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adamos.hubconnector.amqp.AmqpThread;

@Service
public class AmqpService {

	@Autowired
	private AmqpThread threadAmqp;
	
	@Async
	public void restartAmqpSubscription() {
		this.threadAmqp.reconnect();
	}
	
	@Async
	public void disconnectAmqpThread() {
		try {
			this.threadAmqp.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
