package com.nkhang.lending.assessment;

import com.nkhang.lending.application.CreditApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditAssessmentPolicy {
    public record Decision(CreditApplication.Status status, String reasonCode, BigDecimal limit) {}

    public Decision assess(BigDecimal surplus) {
        if (surplus.compareTo(new BigDecimal("400.00")) < 0) {
            return new Decision(CreditApplication.Status.REJECTED, "INSUFFICIENT_SURPLUS", new BigDecimal("0.00"));
        }
        if (surplus.compareTo(new BigDecimal("800.00")) < 0) {
            return approved("APPROVED_TIER_500", "500.00");
        }
        if (surplus.compareTo(new BigDecimal("1500.00")) < 0) {
            return approved("APPROVED_TIER_1000", "1000.00");
        }
        return approved("APPROVED_TIER_2000", "2000.00");
    }

    private Decision approved(String reasonCode, String limit) {
        return new Decision(CreditApplication.Status.APPROVED, reasonCode, new BigDecimal(limit));
    }
}
