package com.adamos.hubconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import com.cumulocity.microservice.context.annotation.EnableContextSupport;
import com.cumulocity.microservice.security.annotation.EnableMicroserviceSecurity;

@MicroserviceApplication
@RestController
@EnableScheduling
@EnableMicroserviceSecurity
@EnableAsync
@EnableContextSupport
public class App {
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public CustomRestTemplateCustomizer customRestTemplateCustomizer() {
	    return new CustomRestTemplateCustomizer();
	}
	
}
