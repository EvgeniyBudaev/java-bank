package com.notificationservice.service;

import com.notificationservice.controller.dto.request.NotificationRequest;
import com.notificationservice.entity.IdempotencyKeyEntity;
import com.notificationservice.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final IdempotencyKeyRepository keyRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Отправить уведомление.
     */
    @Override
    @Transactional
    public void sendNotification(NotificationRequest request) {
        // Пытаемся сохранить ключ идемпотентности
        try {
            keyRepository.save(new IdempotencyKeyEntity(request.idempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            // Если ключ уже есть - считаем операцию успешной (дубль)
            log.warn("Duplicate notification ignored for key: {}", request.idempotencyKey());
            return;
        }

        // Если ключ новый - обрабатываем уведомление
        String timestamp = LocalDateTime.now().format(formatter);
        String message = String.format("Type: %s, Amount: %s, Actor: %s, Target: %s",
                request.type(), request.amount(), request.actorUuid(), request.targetUuid());
        String notification = String.format("[%s] NOTIFICATION: %s", timestamp, message);

        // Выводим в лог
        log.info("Notification: type={} amount={} actor={} target={} at={}",
                request.type(), request.amount(), request.actorUuid(), request.targetUuid(), request.occurredAt());

        // Также выводим в консоль для наглядности
        System.out.println("=".repeat(80));
        System.out.println(notification);
        System.out.println("=".repeat(80));
    }
}
