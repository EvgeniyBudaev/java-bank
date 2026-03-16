package com.cashservice.service;

import com.cashservice.client.AccountsClient;
import com.cashservice.client.NotificationClient;
import com.cashservice.controller.request.RequestCashOperationDto;
import com.cashservice.controller.response.ResponseAccountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Сервис для операций с наличными.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    private final AccountsClient accountsClient;
    private final NotificationClient notificationsClient;

    /**
     * Пополнить счет.
     */
    @Override
    public ResponseAccountDto deposit(RequestCashOperationDto dto) {
        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        ResponseAccountDto account = accountsClient.deposit(dto.userId(), dto.amount());
        sendNotification(new NotificationEvent(
                NotificationType.CASH_IN,
                dto.amount(),
                dto.userId(),
                null,
                Instant.now()
        ));
        return account;
    }

    /**
     * Снять деньги со счета.
     */
    @Override
    public ResponseAccountDto withdraw(RequestCashOperationDto dto) {
        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        ResponseAccountDto account = accountsClient.withdraw(dto.userId(), dto.amount());
        sendNotification(new NotificationEvent(
                NotificationType.CASH_OUT,
                dto.amount(),
                dto.userId(),
                null,
                Instant.now()
        ));
        return account;
    }

    /**
     * Уведомить о событии.
     */
    private void sendNotification(NotificationEvent event) {
        notificationsClient.send(event);
    }
}
