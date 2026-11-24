package com.nkhang.lending.drawdown;

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
@Table(name = "credit_drawdowns")
@Getter
@NoArgsConstructor
public class CreditDrawdown {
    public enum Status { ACTIVE, PAID_OFF }

    @Id
    private UUID id;
    private UUID accountId;
    private BigDecimal originalAmount;
    private BigDecimal outstandingPrincipal;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant createdAt;
    private String idempotencyKey;

    public CreditDrawdown(UUID accountId, BigDecimal amount, String idempotencyKey, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.originalAmount = amount;
        this.outstandingPrincipal = amount;
        this.status = Status.ACTIVE;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;
    }

    public void repay(BigDecimal amount) {
        outstandingPrincipal = outstandingPrincipal.subtract(amount);
        if (outstandingPrincipal.signum() < 0) {
            throw new IllegalStateException("Repayment exceeds outstanding principal");
        }
        if (outstandingPrincipal.signum() == 0) {
            status = Status.PAID_OFF;
        }
    }
}
