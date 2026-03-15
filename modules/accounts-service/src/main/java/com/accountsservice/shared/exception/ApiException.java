package com.accountsservice.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String prodMessage;
    private final String devMessage;

    public ApiException(HttpStatus statusCode, String prodMessage, String devMessage) {
        super(prodMessage);
        this.statusCode = statusCode;
        this.prodMessage = prodMessage;
        this.devMessage = devMessage;
    }
}
