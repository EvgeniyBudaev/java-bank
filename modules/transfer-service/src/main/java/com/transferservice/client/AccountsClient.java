package com.transferservice.client;

import com.transferservice.controller.response.ResponseAccountDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountsClient {
    ResponseAccountDto deposit(UUID userId, BigDecimal amount, String token);

    ResponseAccountDto withdraw(UUID userId, BigDecimal amount, String token);

    ResponseAccountDto refund(UUID userId, BigDecimal amount, String token);
}
