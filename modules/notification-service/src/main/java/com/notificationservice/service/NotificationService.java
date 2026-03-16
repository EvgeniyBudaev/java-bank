package com.notificationservice.service;

import com.notificationservice.controller.dto.request.NotificationRequest;

public interface NotificationService {

    void sendNotification(NotificationRequest request);
}
