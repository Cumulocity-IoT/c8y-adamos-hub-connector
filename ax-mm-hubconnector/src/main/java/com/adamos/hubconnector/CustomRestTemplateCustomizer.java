package com.adamos.hubconnector;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class CustomRestTemplateCustomizer implements RestTemplateCustomizer {
    @Override
    public void customize(RestTemplate restTemplate) {
		// By default a RestTemplate does not use any Connection-Pooling - this leads to the problem
		// that a request always takes about one second, so login, getList, logOff always took 3 seconds+
		// with pooling the same 3 requests took only about 350ms
		HttpClient httpClient = HttpClientBuilder.create()
		         .setMaxConnTotal(5000)
		         .setMaxConnPerRoute(2500)
//		         .setConnectionTimeToLive(0, TimeUnit.SECONDS)
		         .build();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(0);
		restTemplate.setRequestFactory(requestFactory);
    }
}
