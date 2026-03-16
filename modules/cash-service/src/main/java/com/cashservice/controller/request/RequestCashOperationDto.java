package com.cashservice.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record RequestCashOperationDto(
        @NotNull(message = "userId is required")
        UUID userId,
        @NotNull(message = "amount is required")
        @Positive
        BigDecimal amount
) {
}
