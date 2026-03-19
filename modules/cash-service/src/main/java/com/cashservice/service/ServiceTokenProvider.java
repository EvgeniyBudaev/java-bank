package com.cashservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServiceTokenProvider {
    private final OAuth2AuthorizedClientManager manager;

    public ServiceTokenProvider(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );
    }

    /**
     * Получает сервисный токен через Client Credentials flow
     */
    public String getServiceToken() {
        try {
            var request = org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
                    .withClientRegistrationId("cash-service")
                    .principal("cash-service")
                    .build();

            var authorizedClient = manager.authorize(request);

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                return authorizedClient.getAccessToken().getTokenValue();
            }

            log.error("Failed to obtain service token");
            return null;

        } catch (Exception e) {
            log.error("Error obtaining service token", e);
            return null;
        }
    }
}