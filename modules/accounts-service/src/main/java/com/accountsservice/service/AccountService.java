package com.accountsservice.service;

import com.accountsservice.controller.dto.response.ResponseAccountDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    ResponseAccountDto createAccount(UUID userId);

    ResponseAccountDto getAccountByUserId(UUID userId);

    List<ResponseAccountDto> getAllAccounts(UUID excludeUuid);

    ResponseAccountDto deposit(UUID userId, BigDecimal amount);

    ResponseAccountDto withdraw(UUID userId, BigDecimal amount);
}
