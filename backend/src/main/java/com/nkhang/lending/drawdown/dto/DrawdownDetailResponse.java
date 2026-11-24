package com.nkhang.lending.drawdown.dto;

import com.nkhang.lending.drawdown.CreditDrawdown;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DrawdownDetailResponse(UUID drawdownId, BigDecimal originalAmount, BigDecimal outstandingPrincipal,
                                     CreditDrawdown.Status status, Instant createdAt,
                                     List<InstalmentResponse> instalments) {
    public static DrawdownDetailResponse from(CreditDrawdown drawdown, List<InstalmentResponse> instalments) {
        return new DrawdownDetailResponse(drawdown.getId(), drawdown.getOriginalAmount(),
                drawdown.getOutstandingPrincipal(), drawdown.getStatus(), drawdown.getCreatedAt(), instalments);
    }
}
