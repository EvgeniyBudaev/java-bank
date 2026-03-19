package com.cashservice.service;

import com.cashservice.client.AccountsClient;
import com.cashservice.controller.request.RequestCashOperationDto;
import com.cashservice.controller.response.ResponseAccountDto;
import com.cashservice.entity.CashOutboxEntity;
import com.cashservice.exception.InternalServerException;
import com.cashservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
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
    private final OutboxRepository outboxRepository;

    /**
     * Пополнить счет.
     * В рамках одной транзакции: обновляем баланс + сохраняем событие в Outbox.
     */
    @Override
    @Transactional
    public ResponseAccountDto deposit(RequestCashOperationDto dto) {
        String token = getCurrentUserToken();

        // Выполняем критическую операцию (только деньги)
        ResponseAccountDto result = executeWithCircuitBreaker(
                "accounts-deposit",
                () -> accountsClient.deposit(dto.userId(), dto.amount(), token)
        );

        // Сохраняем событие в Outbox (в той же транзакции!)
        saveToOutbox(
                NotificationType.CASH_IN,
                dto.amount(),
                dto.userId(),
                Instant.now()
        );

        return result;
    }

    /**
     * Снять деньги со счета.
     */
    @Override
    @Transactional
    public ResponseAccountDto withdraw(RequestCashOperationDto dto) {
        String token = getCurrentUserToken();

        // Выполняем критическую операцию (только деньги)
        ResponseAccountDto result = executeWithCircuitBreaker(
                "accounts-withdraw",
                () -> accountsClient.withdraw(dto.userId(), dto.amount(), token)
        );

        // Сохраняем событие в Outbox (в той же транзакции!)
        saveToOutbox(
                NotificationType.CASH_OUT,
                dto.amount(),
                dto.userId(),
                Instant.now()
        );

        return result;
    }

    /**
     * Сохраняет событие в таблицу Outbox.
     * Вызывается внутри @Transactional метода, поэтому сохранение происходит
     * в той же транзакции, что и обновление баланса.
     */
    private void saveToOutbox(
            NotificationType type,
            BigDecimal amount,
            UUID userId,
            Instant occurredAt
    ) {
        CashOutboxEntity outbox = new CashOutboxEntity();
        outbox.setType(type);
        outbox.setUserId(userId);
        outbox.setAmount(amount);
        outbox.setCreatedAt(occurredAt);
        outbox.setProcessed(false);

        outboxRepository.save(outbox);

        log.debug("Saved outbox event: type={}, userId={}, amount={}", type, userId, amount);
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
