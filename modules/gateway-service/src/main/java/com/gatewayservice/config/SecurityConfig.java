package com.gatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Конфигурация безопасности для Gateway.
 * Настраивает JWT валидацию и права доступа к эндпоинтам.
 */
@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Публичный доступ
                        .pathMatchers("/actuator/**").permitAll()
                        // Доступно только для SCOPE_accounts.write (POST запросы)
                        .pathMatchers(HttpMethod.POST, "/api/v1/accounts/**")
                        .hasAuthority("SCOPE_accounts.write")
                        // Доступно только для SCOPE_accounts.read (GET и остальные)
                        .pathMatchers("/api/v1/accounts/**").hasAuthority("SCOPE_accounts.read")
                        // Доступно только для SCOPE_cash.write (GET и остальные)
                        .pathMatchers("/api/v1/cash/**").hasAuthority("SCOPE_cash.write")
                        // Остальные запросы требуют аутентификации
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        new ReactiveJwtAuthenticationConverterAdapter(jwtAuthConverter)
                                )
                        )
                )
                .build();
    }
}
