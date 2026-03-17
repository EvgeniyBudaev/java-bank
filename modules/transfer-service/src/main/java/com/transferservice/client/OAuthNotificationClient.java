package com.transferservice.client;

import com.transferservice.service.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OAuthNotificationClient implements NotificationClient {
    private final RestClient notificationRestClient;

    @Override
    public void send(NotificationEvent event, String token) {
        notificationRestClient.post()
                .uri("/api/v1/notifications")
                .header("Authorization", "Bearer " + token)
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }
}
