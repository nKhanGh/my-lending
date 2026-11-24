package com.nkhang.lending.drawdown.dto;

import com.nkhang.lending.drawdown.Instalment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InstalmentResponse(UUID id, int sequenceNo, BigDecimal amount, BigDecimal remainingAmount,
        LocalDate dueDate, Instalment.Status status, int daysPastDue, Instant paidAt) {
    public static InstalmentResponse from(Instalment value) {
        return new InstalmentResponse(value.getId(), value.getSequenceNo(), value.getAmount(),
                value.getRemainingAmount(), value.getDueDate(), value.getStatus(),
                value.getDaysPastDue(), value.getPaidAt());
    }
}
