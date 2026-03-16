package com.notificationservice.service;

import com.notificationservice.controller.dto.request.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Отправить уведомление.
     */
    @Override
    public void sendNotification(NotificationRequest request) {
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
