package com.cashservice.client;

import com.cashservice.service.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OAuthNotificationClient implements NotificationClient {
    private final RestClient notificationRestClient;

    @Override
    public void send(NotificationEvent event) {
        notificationRestClient.post()
                .uri("/api/v1/notifications")
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }
}
