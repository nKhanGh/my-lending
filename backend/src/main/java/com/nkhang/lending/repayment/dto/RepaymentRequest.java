package com.nkhang.lending.repayment.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
public record RepaymentRequest(@DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount) {}
