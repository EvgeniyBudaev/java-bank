package com.notificationservice.controller;

import com.notificationservice.aspect.LogMethodExecutionTime;
import com.notificationservice.controller.dto.request.NotificationRequest;
import com.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для уведомлений.
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Отправить уведомление.
     */
    @PostMapping
    @LogMethodExecutionTime
    @PreAuthorize("hasAuthority('SCOPE_notifications.write')")
    public ResponseEntity<Void> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("NotificationController sendNotification: request={}", request);
        notificationService.sendNotification(request);
        return ResponseEntity.noContent().build();
    }
}
