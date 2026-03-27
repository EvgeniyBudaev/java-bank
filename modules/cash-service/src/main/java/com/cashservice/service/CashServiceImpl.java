package com.cashservice.service;

import com.cashservice.client.AccountsClient;
import com.cashservice.controller.request.RequestCashOperationDto;
import com.cashservice.controller.response.ResponseAccountDto;
import com.cashservice.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Сервис для операций с наличными.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    private final AccountsClient accountsClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    /**
     * Пополнить счет.
     */
    @Override
    public ResponseAccountDto deposit(RequestCashOperationDto dto) {
        String token = getCurrentUserToken();

        // Выполняем критическую операцию (только деньги)
        return executeWithCircuitBreaker(
                "accounts-deposit",
                () -> accountsClient.deposit(dto.userId(), dto.amount(), token)
        );
    }

    /**
     * Снять деньги со счета.
     */
    @Override
    public ResponseAccountDto withdraw(RequestCashOperationDto dto) {
        String token = getCurrentUserToken();

        // Выполняем критическую операцию (только деньги)
        return executeWithCircuitBreaker(
                "accounts-withdraw",
                () -> accountsClient.withdraw(dto.userId(), dto.amount(), token)
        );
    }

    /**
     * Общая логика выполнения операции с защитой CircuitBreaker.
     * Ошибка прерывает выполнение.
     */
    private ResponseAccountDto executeWithCircuitBreaker(
            String circuitBreakerName,
            Supplier<ResponseAccountDto> accountOperation
    ) {
        return circuitBreakerFactory.create(circuitBreakerName).run(
                accountOperation,
                throwable -> {
                    log.error("Circuit breaker opened for {}", circuitBreakerName, throwable);
                    throw new InternalServerException(
                            "Service temporarily unavailable",
                            circuitBreakerName + " circuit breaker: " + throwable.getMessage()
                    );
                }
        );
    }

    /**
     * Получить токен текущего пользователя.
     */
    private String getCurrentUserToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        throw new IllegalStateException("No JWT token found in security context");
    }
}
