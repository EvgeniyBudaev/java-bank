package com.accountsservice.shared.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {
    private static final HttpStatus STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    public InternalServerException(String prodMessage, String devMessage) {
        super(STATUS_CODE, prodMessage, devMessage);
    }
}
