package com.transferservice.service;

import com.transferservice.client.AccountsClient;
import com.transferservice.client.NotificationClient;
import com.transferservice.controller.request.RequestTransferDto;
import com.transferservice.controller.response.ResponseAccountDto;
import com.transferservice.exception.InternalServerException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Сервис для переводов между счетами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {
    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Override
    public ResponseAccountDto transfer(@NotNull UUID fromUserId, RequestTransferDto dto) {
        // 1. Нельзя перевести самому себе
        if (fromUserId.equals(dto.getToUserId())) {
            throw new InternalServerException("Cannot transfer to yourself", "Cannot transfer to yourself");
        }

        // 2. Проверка существования счетов
//        ResponseAccountDto fromAccount = accountsClient.findByUserId(fromUserId)
//                .orElseThrow(() -> new NotFoundException("Sender account not found"));
//
//        ResponseAccountDto toAccount = accountRepository.findByUserId(dto.getToUserId())
//                .orElseThrow(() -> new NotFoundException("Recipient account not found"));

        // 3. Проверка баланса
//        if (fromAccount.getBalance().compareTo(dto.getAmount()) < 0) {
//            throw new InsufficientFundsException("Insufficient funds");
//        }

        String token = getCurrentUserToken();

        try {
            // Списание средств со счёта отправителя
            ResponseAccountDto result = executeWithCircuitBreaker(
                    "accounts-withdraw",
                    () -> accountsClient.withdraw(fromUserId, dto.getAmount(), token)
            );

            // Паттерн Saga (Компенсация)
            try {
                // Зачисление средств на счёт получателя
                ResponseAccountDto r = executeWithCircuitBreaker(
                        "accounts-deposit",
                        () -> accountsClient.deposit(dto.getToUserId(), dto.getAmount(), token)
                );

                // Отправка уведомления ТОЛЬКО ПОСЛЕ успешного завершения обеих операций
                String idempotencyKey = String.valueOf(r.id()); // TODO: нужно id в Outbox таблице. Это для примера компенсационной транзакциию. По хороше нужно сделать в accounts-service
                sendNotification(new NotificationEvent(
                        idempotencyKey,
                        NotificationType.TRANSFER,
                        dto.getAmount(),
                        fromUserId,
                        dto.getToUserId(),
                        Instant.now()
                ), token);

                return result;
            } catch (Exception depositException) {
                // КРИТИЧЕСКИЙ БЛОК: Если зачисление не прошло, нужно вернуть деньги!
                log.error("Deposit failed, initiating compensation (refund) for transfer fromUserId={}",
                        fromUserId, depositException);

                try {
                    // Компенсирующая транзакция: возврат средств
                    executeWithCircuitBreaker(
                            "accounts-refund",
                            () -> accountsClient.refund(fromUserId, dto.getAmount(), token)
                    );
                } catch (Exception refundException) {
                    log.error("COMPENSATION FAILED! Money lost for user {}", fromUserId, refundException);
                    // Здесь нужно отправить алерт админам, так как деньги потеряны
                }

                // Пробрасываем ошибку пользователю
                throw new InternalServerException("Transfer failed", "Deposit error: " + depositException.getMessage());
            }
        } catch (Exception e) {
            log.error("Transfer failed for fromUserId={}, toUserId={}", fromUserId, dto.getToUserId(), e);
            throw new InternalServerException(
                    "Transfer failed",
                    "Transfer error: " + e.getMessage()
            );
        }

    }

    /**
     * Общая логика выполнения операции с защитой CircuitBreaker.
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
     * Отправка уведомления с защитой CircuitBreaker.
     * Ошибка НЕ прерывает выполнение перевода (fallback просто логирует ошибку).
     */
    private void sendNotification(NotificationEvent event, String token) {
        circuitBreakerFactory.create("notifications").run(
                () -> {
                    notificationClient.send(event, token);
                    return null;
                },
                throwable -> {
                    // Fallback: логируем ошибку, но НЕ прерываем перевод
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
