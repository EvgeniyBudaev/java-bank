package com.accountsservice.service;

import com.accountsservice.controller.dto.request.RequestAccountCreateDto;
import com.accountsservice.controller.dto.response.ResponseAccountDto;

import java.util.UUID;

public interface AccountService {
    ResponseAccountDto createAccount(RequestAccountCreateDto dto);

    ResponseAccountDto getAccountByUserId(UUID userId);
}
