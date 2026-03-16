package com.accountsservice.controller.dto.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RequestBalanceChangeDto(@Positive BigDecimal amount) {
}
