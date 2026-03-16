package com.cashservice.client;

import com.cashservice.controller.response.ResponseAccountDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountsClient {
    ResponseAccountDto deposit(UUID userId, BigDecimal amount);

    ResponseAccountDto withdraw(UUID userId, BigDecimal amount);
}
