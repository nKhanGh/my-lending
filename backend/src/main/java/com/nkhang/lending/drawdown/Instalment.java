package com.nkhang.lending.drawdown;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "instalments")
@Getter
@NoArgsConstructor
public class Instalment {
    public enum Status { UNPAID, OVERDUE, PAID }
    @Id private UUID id;
    private UUID drawdownId;
    private int sequenceNo;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) private Status status;
    private Instant paidAt;
    private int daysPastDue;

    public Instalment(UUID drawdownId, int sequenceNo, BigDecimal amount, LocalDate dueDate) {
        id = UUID.randomUUID();
        this.drawdownId = drawdownId;
        this.sequenceNo = sequenceNo;
        this.amount = amount;
        remainingAmount = amount;
        this.dueDate = dueDate;
        status = Status.UNPAID;
    }

    public void applyPayment(BigDecimal payment, Instant now) {
        if (status == Status.PAID || payment.signum() <= 0 || payment.compareTo(remainingAmount) > 0) {
            throw new IllegalStateException("Invalid principal payment");
        }
        remainingAmount = remainingAmount.subtract(payment);
        if (remainingAmount.signum() == 0) {
            status = Status.PAID;
            paidAt = now;
        }
    }

    public void updateOverdue(LocalDate today) {
        if (status == Status.PAID) return;
        daysPastDue = (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, today));
        status = daysPastDue > 0 ? Status.OVERDUE : Status.UNPAID;
    }
}
