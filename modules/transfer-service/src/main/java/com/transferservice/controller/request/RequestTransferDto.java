package com.transferservice.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class RequestTransferDto {
        @NotNull(message = "toUserId is required")
        private UUID toUserId;

        @Positive(message = "amount must be positive")
        private BigDecimal amount;
}