package com.cashservice.controller.response;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ResponseAccountDto(
        Long id,
        @NotBlank(message = "userId is required")
        UUID userId,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {
}
