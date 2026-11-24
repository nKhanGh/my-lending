package com.nkhang.lending.assessment;

import com.nkhang.lending.application.CreditApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

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


    public record ExplainedDecision(Decision decision, BigDecimal debtToIncomeRatio) {}

    public ExplainedDecision assessV2(BigDecimal surplus, BigDecimal monthlyIncome,
                                      BigDecimal monthlyDebtPayments, CreditProfile profile) {
        BigDecimal ratio = monthlyIncome.signum() == 0
                ? (monthlyDebtPayments.signum() == 0 ? BigDecimal.ZERO : BigDecimal.ONE)
                : monthlyDebtPayments.divide(monthlyIncome, 4, RoundingMode.HALF_UP);
        if (profile.getMissedPayments() >= 2) {
            return new ExplainedDecision(new Decision(CreditApplication.Status.REJECTED,
                    "SIMULATED_MISSED_PAYMENTS", BigDecimal.ZERO), ratio);
        }
        if (monthlyDebtPayments.compareTo(monthlyIncome.multiply(new BigDecimal("0.40"))) > 0) {
            return new ExplainedDecision(new Decision(CreditApplication.Status.REJECTED,
                    "DEBT_RATIO_ABOVE_40_PERCENT", BigDecimal.ZERO), ratio);
        }
        return new ExplainedDecision(assess(surplus), ratio);
    }

    private Decision approved(String reasonCode, String limit) {
        return new Decision(CreditApplication.Status.APPROVED, reasonCode, new BigDecimal(limit));
    }
}
