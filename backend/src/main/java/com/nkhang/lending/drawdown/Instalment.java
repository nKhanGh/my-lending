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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "instalments")
@Getter
@NoArgsConstructor
public class Instalment {
    public enum Status { UNPAID, PAID }

    @Id
    private UUID id;
    private UUID drawdownId;
    private int sequenceNo;
    private BigDecimal amount;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant paidAt;

    public Instalment(UUID drawdownId, int sequenceNo, BigDecimal amount, LocalDate dueDate) {
        this.id = UUID.randomUUID();
        this.drawdownId = drawdownId;
        this.sequenceNo = sequenceNo;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = Status.UNPAID;
    }

    public void markPaid(Instant time) {
        if (status != Status.UNPAID) {
            throw new IllegalStateException("Instalment already paid");
        }
        status = Status.PAID;
        paidAt = time;
    }
}
