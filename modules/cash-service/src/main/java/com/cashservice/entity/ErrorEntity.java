package com.cashservice.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorEntity {
    private String serviceName;
    private Integer statusCode;
    private Boolean success;
    private String prodMessage;
    private String devMessage;
    private Instant errorTime;
}
