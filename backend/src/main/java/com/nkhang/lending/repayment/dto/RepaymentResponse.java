package com.nkhang.lending.repayment.dto;

import com.nkhang.lending.repayment.RepaymentTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RepaymentResponse(UUID repaymentId, BigDecimal amount, RepaymentTransaction.Status status,
        Instant requestedAt, Instant resolvedAt, BigDecimal remainingDrawdownPrincipal, BigDecimal availableLimit) {
    public static RepaymentResponse from(RepaymentTransaction payment, BigDecimal remaining, BigDecimal available) {
        return new RepaymentResponse(payment.getId(), payment.getAmount(), payment.getStatus(),
                payment.getRequestedAt(), payment.getPaidAt(), remaining, available);
    }
}
