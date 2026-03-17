package com.transferservice.client;

import com.transferservice.service.NotificationEvent;

public interface NotificationClient {
    void send(NotificationEvent event, String token);
}
