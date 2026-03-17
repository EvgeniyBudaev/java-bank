package com.cashservice.client;

import com.cashservice.service.NotificationEvent;

public interface NotificationClient {
    void send(NotificationEvent event, String token);
}
