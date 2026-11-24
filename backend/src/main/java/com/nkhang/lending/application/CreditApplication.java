package com.nkhang.lending.application;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_applications")
@Getter
@NoArgsConstructor
public class CreditApplication {
    public enum Status { APPROVED, REJECTED }

    @Id
    private UUID id;
    private UUID userId;
    private BigDecimal monthlyNetIncome;
    private BigDecimal livingExpenses;
    private BigDecimal debtPayments;
    private BigDecimal surplus;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String decisionReason;
    private String policyVersion;
    private BigDecimal approvedLimit;
    private Integer onTimePayments;
    private Integer missedPayments;
    private BigDecimal debtToIncomeRatio;
    private Instant assessedAt;

    public CreditApplication(UUID userId, BigDecimal monthlyNetIncome, BigDecimal livingExpenses,
                             BigDecimal debtPayments, BigDecimal surplus, Status status,
                             String decisionReason, BigDecimal approvedLimit, Instant assessedAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.monthlyNetIncome = monthlyNetIncome;
        this.livingExpenses = livingExpenses;
        this.debtPayments = debtPayments;
        this.surplus = surplus;
        this.status = status;
        this.decisionReason = decisionReason;
        this.policyVersion = "DEMO_V1";
        this.approvedLimit = approvedLimit;
        this.assessedAt = assessedAt;
    }

    public void recordSimulatedAssessment(CreditProfileSnapshot profile, BigDecimal ratio) {
        policyVersion = "DEMO_V2";
        onTimePayments = profile.onTimePayments();
        missedPayments = profile.missedPayments();
        debtToIncomeRatio = ratio;
    }

    public record CreditProfileSnapshot(int onTimePayments, int missedPayments) {}
}
