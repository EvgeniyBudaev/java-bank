package com.cashservice.controller;

import com.cashservice.aspect.LogMethodExecutionTime;
import com.cashservice.controller.request.RequestCashOperationDto;
import com.cashservice.controller.response.ResponseAccountDto;
import com.cashservice.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-контроллер для работы с наличными.
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/cash")
public class CashController {
    private final CashService cashService;

    /**
     * Пополнить баланс.
     */
    @PostMapping("/deposit")
    @LogMethodExecutionTime
    public ResponseEntity<ResponseAccountDto> deposit(
            @Valid @RequestBody RequestCashOperationDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        log.info("CashController deposit: uuid={}", uuid);
        return ResponseEntity.ok(cashService.deposit(dto));
    }

    /**
     * Снять средства с баланса.
     */
    @PostMapping("/withdraw")
    @LogMethodExecutionTime
    public ResponseEntity<ResponseAccountDto> withdraw(
            @Valid @RequestBody RequestCashOperationDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        log.info("CashController withdraw: uuid={}", uuid);
        return ResponseEntity.ok(cashService.withdraw(dto));
    }
}
