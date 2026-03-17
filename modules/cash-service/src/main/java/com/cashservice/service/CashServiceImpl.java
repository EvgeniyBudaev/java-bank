package com.cashservice.service;

import com.cashservice.client.AccountsClient;
import com.cashservice.client.NotificationClient;
import com.cashservice.controller.request.RequestCashOperationDto;
import com.cashservice.controller.response.ResponseAccountDto;
import com.cashservice.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Сервис для операций с наличными.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    /**
     * Пополнить счет.
     */
    @Override
    public ResponseAccountDto deposit(RequestCashOperationDto dto) {
        String token = getCurrentUserToken();

        // Выполняем критическую операцию (только деньги)
        ResponseAccountDto result = executeWithCircuitBreaker(
                "accounts-deposit",
                () -> accountsClient.deposit(dto.userId(), dto.amount(), token)
        );

        // Уведомление выполняем ОТДЕЛЬНО (не влияет на результат)
        sendNotification(
                new NotificationEvent(
                        NotificationType.CASH_IN,
                        dto.amount(),
                        dto.userId(),
                        null,
                        Instant.now()
                ),
                token
        );

        return result;
    }

    /**
     * Снять деньги со счета.
     */
    @Override
    public ResponseAccountDto withdraw(RequestCashOperationDto dto) {
        String token = getCurrentUserToken();

        // Выполняем критическую операцию (только деньги)
        ResponseAccountDto result = executeWithCircuitBreaker(
                "accounts-withdraw",
                () -> accountsClient.withdraw(dto.userId(), dto.amount(), token)
        );

        // Уведомление выполняем ОТДЕЛЬНО
        sendNotification(
                new NotificationEvent(
                        NotificationType.CASH_OUT,
                        dto.amount(),
                        dto.userId(),
                        null,
                        Instant.now()
                ),
                token
        );

        return result;
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
     * Уведомить о событии.
     * Безопасная отправка уведомления. Ошибка НЕ прерывает основной поток.
     */
    private void sendNotification(NotificationEvent event, String token) {
        circuitBreakerFactory.create("notifications").run(
                () -> {
                    notificationClient.send(event, token);
                    return null;
                },
                throwable -> {
                    // Глотаем ошибку, только логируем
                    log.warn("Failed to send notification (non-critical)", throwable);
                    return null;
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
