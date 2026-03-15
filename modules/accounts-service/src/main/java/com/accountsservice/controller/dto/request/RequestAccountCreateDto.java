package com.accountsservice.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestAccountCreateDto(
        @NotNull(message = "userId is required")
        UUID userId
) {
}
