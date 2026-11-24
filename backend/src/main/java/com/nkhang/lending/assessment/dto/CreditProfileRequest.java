package com.nkhang.lending.assessment.dto;
import jakarta.validation.constraints.Min;
public record CreditProfileRequest(@Min(0) int onTimePayments, @Min(0) int missedPayments) {}
