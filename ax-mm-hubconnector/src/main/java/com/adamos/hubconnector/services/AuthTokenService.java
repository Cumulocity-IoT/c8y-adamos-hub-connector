package com.adamos.hubconnector.services;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.adamos.hubconnector.model.HubConnectorGlobalSettings;
import com.adamos.hubconnector.model.OAuth2Credentials;
import com.adamos.hubconnector.model.OAuth2Token;
import com.cumulocity.sdk.client.inventory.InventoryApi;

@Service
public class AuthTokenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CumulocityService.class);

    @Autowired
    InventoryApi inventoryApi;

    @Autowired
    private HubConnectorService hubConnectorService;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private OAuth2Token currentToken;

    public void setCurrentToken(OAuth2Token token) {
        writeLock.lock();
        try {
            this.currentToken = token;
        } finally {
            writeLock.unlock();
        }
    }

    public OAuth2Token getCurrentToken() {
        readLock.lock();
        try {
            return this.currentToken;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Generates an OAuth2 token with the help of a refreshtoken.
     * Currently our Keycloak needs additional Basic Auth credentials
     * 
     * @param refreshToken
     * @param secret
     * @param realm
     * @param username
     * @return
     */
    private OAuth2Token getNewTokenViaRefreshToken(String refreshToken, OAuth2Credentials credentials) {
        LOGGER.info("Getting new OAuth2 token via refresh token");
        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        credentials.setGrant_type("refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.put("client_id", Collections.singletonList(credentials.getClient_id()));
        body.put("client_secret", Collections.singletonList(credentials.getClient_secret()));
        body.put("grant-type", Collections.singletonList(credentials.getGrant_type()));
        RequestEntity<LinkedMultiValueMap<String, String>> requestEntity = new RequestEntity<LinkedMultiValueMap<String, String>>(
                body, headers, HttpMethod.POST, URI.create(hubConnectorService.getGlobalSettings().getOAuthEndpoint()));
        ResponseEntity<OAuth2Token> responseEntity = rest.exchange(requestEntity, OAuth2Token.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            OAuth2Token token = responseEntity.getBody();
            token.setExpiryDate(DateTime.now().plus(token.getExpiresIn() - 10));
            return token;
        }
        LOGGER.error("OAuthException error trying to retrieve access token");
        throw new RuntimeException("error trying to retrieve access token");
    }

    private OAuth2Token getNewToken(OAuth2Credentials credentials) {
        LOGGER.info("Getting new OAuth2 token");
        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        credentials.setGrant_type("client_credentials");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.put("client_id", Collections.singletonList(credentials.getClient_id()));
        body.put("client_secret", Collections.singletonList(credentials.getClient_secret()));
        body.put("grant-type", Collections.singletonList(credentials.getGrant_type()));
        RequestEntity<LinkedMultiValueMap<String, String>> requestEntity = new RequestEntity<LinkedMultiValueMap<String, String>>(
                body, headers, HttpMethod.POST, URI.create(hubConnectorService.getGlobalSettings().getOAuthEndpoint()));
        ResponseEntity<OAuth2Token> responseEntity = rest.exchange(requestEntity, OAuth2Token.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            OAuth2Token token = responseEntity.getBody();
            token.setExpiryDate(DateTime.now().plus(token.getExpiresIn() - 10));
            return token;
        }
        LOGGER.error("OAuthException error trying to retrieve access token");
        throw new RuntimeException("error trying to retrieve access token");
    }

    public OAuth2Token getToken() {
        // get globalSettings - this object is always created with the first
        // subscription of the service!
        HubConnectorGlobalSettings globalSettings = hubConnectorService.getGlobalSettings();

        if (!globalSettings.getOAuth2Credentials().initialized()) {
            return null;
        }

        String secret = globalSettings.getOAuth2Credentials().getClient_secret();
        OAuth2Credentials body = globalSettings.getOAuth2Credentials().clone();
        body.setClient_secret(secret);

        if (this.getCurrentToken() == null || this.getCurrentToken().getAccessToken() == null) {
            OAuth2Token token = getNewToken(body);
            setCurrentToken(token);
        } else {
            OAuth2Token token = getCurrentToken();
            if (DateTime.now().isAfter(token.getExpiryDate())) {
                try {
                    // get a new token with the help of the refresh-token we got with the last
                    // session
                    // token = getNewTokenViaRefreshToken(token.getRefreshToken(), body);
                    token = getNewToken(body);
                } catch (org.springframework.web.client.HttpClientErrorException ex) {
                    LOGGER.error("Error while trying to get a new token via refresh token.", ex);
                    if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        LOGGER.info("StatusCode 400 -> trying to get a new token without refresh token...");
                        token = getNewToken(body);
                        LOGGER.info("Retry without refresh token has been successful...");
                    }
                }
                setCurrentToken(token);
                return token;
            }
        }
        return getCurrentToken();
    }

    public HttpHeaders getHeaderBearerToken(MediaType contentType) {
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(contentType);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        httpHeaders.add("Authorization", "Bearer " + getToken().getAccessToken());

        return httpHeaders;
    }

}