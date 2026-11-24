package com.nkhang.lending.repayment.dto;

import com.nkhang.lending.drawdown.Instalment;
import com.nkhang.lending.repayment.RepaymentTransaction;

import java.math.BigDecimal;
import java.util.UUID;

public record RepaymentResponse(UUID repaymentId, BigDecimal paidAmount,
                                BigDecimal remainingDrawdownPrincipal, BigDecimal availableLimit,
                                Instalment.Status instalmentStatus) {
    public static RepaymentResponse from(RepaymentTransaction repayment, BigDecimal remaining,
                                         BigDecimal available) {
        return new RepaymentResponse(repayment.getId(), repayment.getAmount(), remaining, available,
                Instalment.Status.PAID);
    }
}
