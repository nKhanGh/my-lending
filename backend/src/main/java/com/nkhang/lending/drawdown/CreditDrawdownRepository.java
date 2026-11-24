package com.nkhang.lending.drawdown;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditDrawdownRepository extends JpaRepository<CreditDrawdown, UUID> {
    Optional<CreditDrawdown> findByAccountIdAndIdempotencyKey(UUID accountId, String idempotencyKey);
    Optional<CreditDrawdown> findByIdAndAccountId(UUID id, UUID accountId);
    List<CreditDrawdown> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
