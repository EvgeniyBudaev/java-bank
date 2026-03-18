package com.cashservice.config;

import com.commonservice.config.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности для Cash Service.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
@Import(com.commonservice.config.CommonSecurityConfig.class)
public class SecurityConfig {
    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                // Публичный доступ (незалогиненные пользователи)
                                .requestMatchers("/actuator/**").permitAll()
                                // Доступно только для SCOPE_cash.write
                                .requestMatchers("/api/v1/cash/**").hasAuthority("SCOPE_cash.write")
                                // Остальные запросы требуют аутентификации
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
