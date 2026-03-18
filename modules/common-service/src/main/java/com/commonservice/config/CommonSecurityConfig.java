package com.commonservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonSecurityConfig {

    @Bean
    public JwtAuthConverter jwtAuthConverter() {
        return new JwtAuthConverter();
    }
}