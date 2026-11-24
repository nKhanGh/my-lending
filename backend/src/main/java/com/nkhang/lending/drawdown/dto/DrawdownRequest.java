package com.nkhang.lending.drawdown.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DrawdownRequest(@NotNull @DecimalMin("0.04") @Digits(integer = 17, fraction = 2) BigDecimal amount) {
}
