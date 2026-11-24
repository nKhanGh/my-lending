package com.nkhang.lending.repayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SettlePaymentRequest(
        @NotBlank @Pattern(regexp = "SUCCESS|FAILED") String outcome,
        @NotBlank String providerEventId) {}
