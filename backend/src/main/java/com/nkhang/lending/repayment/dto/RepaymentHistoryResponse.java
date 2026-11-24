package com.nkhang.lending.repayment.dto;

import com.nkhang.lending.repayment.RepaymentTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RepaymentHistoryResponse(UUID id, UUID instalmentId, BigDecimal amount,
        RepaymentTransaction.Status status, Instant requestedAt, Instant paidAt) {
    public static RepaymentHistoryResponse from(RepaymentTransaction payment) {
        return new RepaymentHistoryResponse(payment.getId(), payment.getInstalmentId(),
                payment.getAmount(), payment.getStatus(), payment.getRequestedAt(), payment.getPaidAt());
    }
}
