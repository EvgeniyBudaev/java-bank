package com.transferservice.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
        @NotNull NotificationType type,
        @PositiveOrZero BigDecimal amount,
        @NotNull UUID actorUuid,
        UUID targetUuid,
        @NotNull Instant occurredAt
) {
}
