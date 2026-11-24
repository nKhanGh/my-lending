package com.nkhang.lending.repayment.dto;

import com.nkhang.lending.repayment.RepaymentTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RepaymentHistoryResponse(UUID id, UUID instalmentId, BigDecimal amount,
                                       RepaymentTransaction.Status status, Instant paidAt) {
    public static RepaymentHistoryResponse from(RepaymentTransaction repayment) {
        return new RepaymentHistoryResponse(repayment.getId(), repayment.getInstalmentId(),
                repayment.getAmount(), repayment.getStatus(), repayment.getPaidAt());
    }
}
