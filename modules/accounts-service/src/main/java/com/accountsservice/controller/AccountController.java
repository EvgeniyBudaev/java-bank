package com.accountsservice.controller;

import com.accountsservice.aspect.LogMethodExecutionTime;
import com.accountsservice.controller.dto.request.RequestBalanceChangeDto;
import com.accountsservice.controller.dto.response.ResponseAccountDto;
import com.accountsservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<ResponseAccountDto> createAccount(@AuthenticationPrincipal Jwt jwt) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        log.info("AccountController createAccount: uuid={}", uuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(uuid));
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
        return ResponseEntity.ok(accountService.getAccountByUserId(uuid));
    }

    /**
     * Получить список всех аккаунтов (кроме текущего пользователя).
     */
    @GetMapping("/all")
    @LogMethodExecutionTime
    @PreAuthorize("hasAuthority('SCOPE_accounts.read')")
    public ResponseEntity<List<ResponseAccountDto>> getAllAccounts(@AuthenticationPrincipal Jwt jwt) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        log.info("AccountController getAllAccounts: uuid={}", uuid);
        return ResponseEntity.ok(accountService.getAllAccounts(uuid));
    }

    /**
     * Пополнить баланс (используется Cash Service).
     */
    @PostMapping("/{uuid}/deposit")
    @LogMethodExecutionTime
    public ResponseEntity<ResponseAccountDto> deposit(
            @PathVariable UUID uuid,
            @RequestBody RequestBalanceChangeDto dto) {
        log.info("AccountController deposit: uuid={}, dto={}", uuid, dto);
        return ResponseEntity.ok(accountService.deposit(uuid, dto.amount()));
    }

    /**
     * Снять средства с баланса (используется Cash Service и Transfer Service).
     */
    @PostMapping("/{uuid}/withdraw")
    public ResponseEntity<ResponseAccountDto> withdraw(
            @PathVariable UUID uuid,
            @RequestBody RequestBalanceChangeDto dto) {
        log.info("AccountController withdraw: uuid={}, dto={}", uuid, dto);
        return ResponseEntity.ok(accountService.withdraw(uuid, dto.amount()));
    }
}
