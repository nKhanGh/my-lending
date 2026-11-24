package com.nkhang.lending;

import com.nkhang.lending.application.CreditApplication;
import com.nkhang.lending.assessment.CreditAssessmentPolicy;
import com.nkhang.lending.assessment.CreditProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreditAssessmentPolicyTest {
    private final CreditAssessmentPolicy policy = new CreditAssessmentPolicy();

    @Test
    void matchesAllPolicyBoundaries() {
        assertDecision("399.99", CreditApplication.Status.REJECTED, "INSUFFICIENT_SURPLUS", "0.00");
        assertDecision("400.00", CreditApplication.Status.APPROVED, "APPROVED_TIER_500", "500.00");
        assertDecision("800.00", CreditApplication.Status.APPROVED, "APPROVED_TIER_1000", "1000.00");
        assertDecision("1500.00", CreditApplication.Status.APPROVED, "APPROVED_TIER_2000", "2000.00");
    }

    @Test
    void demoV2ExplainsDebtRatioAndMissedHistory() {
        var clean = new CreditProfile(java.util.UUID.randomUUID(), 5, 0, java.time.Instant.EPOCH);
        var missed = new CreditProfile(java.util.UUID.randomUUID(), 5, 2, java.time.Instant.EPOCH);
        var accepted = policy.assessV2(new BigDecimal("800.00"), new BigDecimal("3000.00"),
                new BigDecimal("1200.00"), clean);
        assertEquals("APPROVED_TIER_1000", accepted.decision().reasonCode());
        assertEquals(new BigDecimal("0.4000"), accepted.debtToIncomeRatio());
        var debtRejected = policy.assessV2(new BigDecimal("800.00"), new BigDecimal("3000.00"),
                new BigDecimal("1200.01"), clean);
        assertEquals("DEBT_RATIO_ABOVE_40_PERCENT", debtRejected.decision().reasonCode());
        var historyRejected = policy.assessV2(new BigDecimal("800.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, missed);
        assertEquals("SIMULATED_MISSED_PAYMENTS", historyRejected.decision().reasonCode());
    }

    private void assertDecision(String surplus, CreditApplication.Status status, String reason, String limit) {
        var decision = policy.assess(new BigDecimal(surplus));
        assertEquals(status, decision.status());
        assertEquals(reason, decision.reasonCode());
        assertEquals(0, new BigDecimal(limit).compareTo(decision.limit()));
    }
}
