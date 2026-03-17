package com.transferservice.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {
    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public InsufficientFundsException(String prodMessage, String devMessage) {
        super(STATUS_CODE, prodMessage, devMessage);
    }
}
