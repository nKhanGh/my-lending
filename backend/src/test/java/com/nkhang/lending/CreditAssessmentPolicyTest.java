package com.nkhang.lending;

import com.nkhang.lending.application.CreditApplication;
import com.nkhang.lending.assessment.CreditAssessmentPolicy;
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

    private void assertDecision(String surplus, CreditApplication.Status status, String reason, String limit) {
        var decision = policy.assess(new BigDecimal(surplus));
        assertEquals(status, decision.status());
        assertEquals(reason, decision.reasonCode());
        assertEquals(0, new BigDecimal(limit).compareTo(decision.limit()));
    }
}
