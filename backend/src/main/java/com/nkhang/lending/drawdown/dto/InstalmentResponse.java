package com.nkhang.lending.drawdown.dto;

import com.nkhang.lending.drawdown.Instalment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InstalmentResponse(UUID id, int sequenceNo, BigDecimal amount, LocalDate dueDate,
                                Instalment.Status status, Instant paidAt) {
    public static InstalmentResponse from(Instalment instalment) {
        return new InstalmentResponse(instalment.getId(), instalment.getSequenceNo(), instalment.getAmount(),
                instalment.getDueDate(), instalment.getStatus(), instalment.getPaidAt());
    }
}
