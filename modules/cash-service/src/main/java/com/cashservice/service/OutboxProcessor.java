package com.cashservice.service;

import com.cashservice.client.NotificationClient;
import com.cashservice.entity.CashOutboxEntity;
import com.cashservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final NotificationClient notificationClient;
    private final ServiceTokenProvider tokenProvider;

    @Value("${spring.application.outbox.limit:10}")
    private int limit;

    /**
     * Обрабатывает необработанные записи из Outbox.
     * Запускается каждую секунду.
     */
    @Scheduled(fixedDelayString = "PT30S")
    @Transactional
    public void processOutbox() {
        // Получаем ТОЛЬКО необработанные записи
        List<CashOutboxEntity> outboxEntries = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc(
                Pageable.ofSize(limit)
        );

        if (outboxEntries.isEmpty()) {
            log.debug("No unprocessed outbox entries found");
            return;
        }

        for (CashOutboxEntity entry : outboxEntries) {
            try {
                String token = tokenProvider.getServiceToken();
                String idempotencyKey = String.valueOf(entry.getId());

                // Отправляем уведомление с CircuitBreaker
                boolean success = sendNotification(
                        new NotificationEvent(
                                idempotencyKey,
                                entry.getType(),
                                entry.getAmount(),
                                entry.getUserId(),
                                null,
                                entry.getCreatedAt()
                        ),
                        token
                );

                // Удаляем запись ТОЛЬКО если отправка успешна
                if (success) {
                    outboxRepository.deleteById(entry.getId());
                    log.info("Successfully processed and deleted outbox entry: id={}, type={}",
                            entry.getId(), entry.getType());
                } else {
                    log.warn("Notification failed, keeping outbox entry for retry: id={}, type={}",
                            entry.getId(), entry.getType());
                }
            } catch (Exception e) {
                // НЕ помечаем как обработанную! Будет повторная попытка в следующем цикле
                log.error("Failed to process outbox entry: id={}, type={}",
                        entry.getId(), entry.getType(), e);
                // Можно добавить счетчик попыток и логику для "отравленных" сообщений
            }
        }
    }

    /**
     * Уведомить о событии.
     * Безопасная отправка уведомления. Ошибка НЕ прерывает основной поток.
     * @return true если успешно, false если ошибка
     */
    private boolean sendNotification(NotificationEvent event, String token) {
        return circuitBreakerFactory.create("notifications").run(
                () -> {
                    notificationClient.send(event, token);
                    return true;
                },
                throwable -> {
                    // Глотаем ошибку, только логируем
                    log.warn("Failed to send notification (non-critical)", throwable);
                    return false;
                }
        );
    }
}
