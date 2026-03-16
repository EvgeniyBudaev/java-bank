package com.accountsservice.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public BadRequestException(String prodMessage, String devMessage) {
        super(STATUS_CODE, prodMessage, devMessage);
    }
}
