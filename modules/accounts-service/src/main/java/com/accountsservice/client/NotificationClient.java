package com.accountsservice.client;

import com.accountsservice.service.NotificationEvent;

public interface NotificationClient {
    void send(NotificationEvent event, String token);
}
