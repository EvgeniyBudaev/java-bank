package com.transferservice.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    private static final HttpStatus STATUS_CODE = HttpStatus.NOT_FOUND;

    public NotFoundException(String prodMessage, String devMessage) {
        super(STATUS_CODE, prodMessage, devMessage);
    }
}
