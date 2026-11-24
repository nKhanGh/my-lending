package com.nkhang.lending.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreditApplicationRequest(
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal monthlyNetIncome,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal monthlyLivingExpenses,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal monthlyDebtPayments,
        String currency) {
}
