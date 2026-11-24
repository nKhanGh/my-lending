package com.nkhang.lending.application.dto;

import com.nkhang.lending.application.CreditApplication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditApplicationResponse(UUID applicationId, CreditApplication.Status decision,
                                        String reasonCode, String policyVersion, BigDecimal monthlySurplus,
                                        BigDecimal approvedLimit, String currency, Instant assessedAt,
                                        Integer onTimePayments, Integer missedPayments, BigDecimal debtToIncomeRatio) {
    public static CreditApplicationResponse from(CreditApplication application) {
        return new CreditApplicationResponse(application.getId(), application.getStatus(),
                application.getDecisionReason(), application.getPolicyVersion(), application.getSurplus(),
                application.getApprovedLimit(), "SGD", application.getAssessedAt(),
                application.getOnTimePayments(), application.getMissedPayments(), application.getDebtToIncomeRatio());
    }
}
