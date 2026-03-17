package com.transferservice.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

/**
 * Конфигурация OAuth2 Client для межсервисного взаимодействия.
 */
@Configuration
public class OAuth2RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient accountsRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://accounts-service")
                .requestInterceptor(createTokenRelayInterceptor())
                .build();
    }

    @Bean
    public RestClient notificationRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://notification-service")
                .requestInterceptor(createTokenRelayInterceptor())
                .build();
    }

    private ClientHttpRequestInterceptor createTokenRelayInterceptor() {
        return (request, body, execution) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                request.getHeaders().setBearerAuth(token);
            }

            return execution.execute(request, body);
        };
    }
}
