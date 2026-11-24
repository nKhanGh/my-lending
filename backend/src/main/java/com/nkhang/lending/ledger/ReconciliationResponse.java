package com.nkhang.lending.ledger;
import java.math.BigDecimal;
import java.util.UUID;
public record ReconciliationResponse(UUID accountId, UUID userId, BigDecimal accountUsed,
        BigDecimal drawdownOutstanding, BigDecimal instalmentRemaining, BigDecimal ledgerNet,
        boolean matched) {}
