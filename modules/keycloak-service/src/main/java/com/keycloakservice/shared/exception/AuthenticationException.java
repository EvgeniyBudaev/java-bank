package com.keycloakservice.shared.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {
    private static final HttpStatus STATUS_CODE = HttpStatus.UNAUTHORIZED;

    public AuthenticationException(String prodMessage, String devMessage) {
        super(STATUS_CODE, prodMessage, devMessage);
    }
}