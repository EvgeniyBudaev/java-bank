package com.accountsservice.controller;

import com.accountsservice.aspect.LogMethodExecutionTime;
import com.accountsservice.controller.dto.request.RequestAccountCreateDto;
import com.accountsservice.controller.dto.response.ResponseAccountDto;
import com.accountsservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-контроллер для работы с аккаунтами.
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    /**
     * Создать новый аккаунт.
     * Доступно только для сервисов/админов с правом accounts.write
     */
    @PostMapping
    @LogMethodExecutionTime
    @PreAuthorize("hasAuthority('SCOPE_accounts.write')")
    public ResponseEntity<ResponseAccountDto> createAccount(@RequestBody @Valid RequestAccountCreateDto dto) {
        log.info("AccountController createAccount: dto={}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(dto));
    }

    /**
     * Получить данные своего аккаунта.
     */
    @GetMapping("/me")
    @LogMethodExecutionTime
    @PreAuthorize("hasAuthority('SCOPE_accounts.read')")
    public ResponseEntity<ResponseAccountDto> getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        log.info("AccountController getMyAccount: uuid={}", uuid);
        ResponseAccountDto account = accountService.getAccountByUserId(uuid);
        return ResponseEntity.ok(account);
    }
}
