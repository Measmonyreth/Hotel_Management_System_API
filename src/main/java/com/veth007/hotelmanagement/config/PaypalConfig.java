package com.veth007.hotelmanagement.config;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

@Configuration
public class PaypalConfig {
    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public Map<String, String> payPalSDKConfig() {
        Map<String, String> config = new HashMap<String, String>();
        config.put("mode", mode);
        return config;
    }

    @Bean
    public OAuthTokenCredential oAuthTokenCredential() {
        return new OAuthTokenCredential(clientId, clientSecret, payPalSDKConfig());
    }

    @Bean
    public APIContext apiContext() throws PayPalRESTException {
        // Fetches a real access token from PayPal using the credentials above.
        // This makes a network call at startup and requires valid sandbox credentials.
        APIContext apiContext = new APIContext(oAuthTokenCredential().getAccessToken());
        apiContext.setConfigurationMap(payPalSDKConfig());
        return apiContext;
    }
}

