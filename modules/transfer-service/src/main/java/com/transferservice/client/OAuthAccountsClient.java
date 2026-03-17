package com.transferservice.client;

import com.transferservice.controller.response.ResponseAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthAccountsClient implements AccountsClient {
    private final RestClient accountsRestClient;

    @Override
    public ResponseAccountDto deposit(UUID userId, BigDecimal amount, String token) {
        return accountsRestClient.post()
                .uri("/api/v1/accounts/{userId}/deposit", userId)
                .header("Authorization", "Bearer " + token)
                .body(new AmountDto(amount))
                .retrieve()
                .body(ResponseAccountDto.class);
    }

    @Override
    public ResponseAccountDto withdraw(UUID userId, BigDecimal amount, String token) {
        return accountsRestClient.post()
                .uri("/api/v1/accounts/{userId}/withdraw", userId)
                .header("Authorization", "Bearer " + token)
                .body(new AmountDto(amount))
                .retrieve()
                .body(ResponseAccountDto.class);
    }

    @Override
    public ResponseAccountDto refund(UUID userId, BigDecimal amount, String token) {
        return accountsRestClient.post()
                .uri("/api/v1/accounts/{userId}/refund", userId)
                .header("Authorization", "Bearer " + token)
                .body(new AmountDto(amount))
                .retrieve()
                .body(ResponseAccountDto.class);
    }

    private record AmountDto(BigDecimal amount) {
    }
}
