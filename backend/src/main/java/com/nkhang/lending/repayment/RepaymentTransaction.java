package com.nkhang.lending.repayment;

import jakarta.persistence.*;
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
    public enum Status { PENDING, SUCCESS, FAILED }
    @Id private UUID id;
    private UUID userId;
    private UUID instalmentId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING) private Status status;
    private Instant requestedAt;
    private Instant paidAt;
    private String providerEventId;
    private String idempotencyKey;

    public RepaymentTransaction(UUID userId, UUID instalmentId, BigDecimal amount, Instant now, String key) {
        id = UUID.randomUUID();
        this.userId = userId;
        this.instalmentId = instalmentId;
        this.amount = amount;
        status = Status.PENDING;
        requestedAt = now;
        idempotencyKey = key;
    }

    public void resolve(Status outcome, String eventId, Instant now) {
        if (status != Status.PENDING || outcome == Status.PENDING) throw new IllegalStateException("Invalid payment transition");
        status = outcome;
        providerEventId = eventId;
        paidAt = now;
    }
}
