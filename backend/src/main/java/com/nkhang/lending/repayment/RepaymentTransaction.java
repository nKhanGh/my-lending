package com.nkhang.lending.repayment;

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
@Table(name = "repayment_transactions")
@Getter
@NoArgsConstructor
public class RepaymentTransaction {
    public enum Status { SUCCESS }

    @Id
    private UUID id;
    private UUID instalmentId;
    private UUID userId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant paidAt;
    private String idempotencyKey;

    public RepaymentTransaction(UUID userId, UUID instalmentId, BigDecimal amount, Instant paidAt, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.instalmentId = instalmentId;
        this.userId = userId;
        this.amount = amount;
        this.status = Status.SUCCESS;
        this.paidAt = paidAt;
        this.idempotencyKey = idempotencyKey;
    }
}
