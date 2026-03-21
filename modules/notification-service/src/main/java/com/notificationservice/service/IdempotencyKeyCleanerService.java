package com.notificationservice.service;

import com.notificationservice.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyKeyCleanerService {
    private final IdempotencyKeyRepository keyRepository;

    @Value("${notification.idempotency.retention-days:7}")
    private int retentionDays;

    @Value("${notification.idempotency.retention-minutes:0}")
    private int retentionMinutes;

    /**
     * Запускается каждый день в 03:00 ночи.
     * Удаляет ключи, созданные более чем ${retentionDays} дней назад.
     * Пакетное удаление: Если ожидается очень большое количество записей (миллионы),
     * то лучше переписать на удалление записей пакетами, чтобы не блокировать таблицу надолго.
     */
//    @Scheduled(cron = "0 0 3 * * ?")
    @Scheduled(fixedDelay = 60000) // 60000 мс = 1 минута
    @Transactional
    public void cleanupExpiredKeys() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusDays(retentionDays)
                .minusMinutes(retentionMinutes);

        log.info("Starting idempotency keys cleanup (older than {} days)", retentionDays);

        try {
            int deletedCount = keyRepository.deleteByCreatedAtBefore(threshold);

            if (deletedCount > 0) {
                log.info("Cleanup completed. Deleted {} expired idempotency keys", deletedCount);
            } else {
                log.info("No expired keys found for cleanup");
            }
        } catch (Exception e) {
            log.error("Error during idempotency keys cleanup", e);
        }
    }
}
