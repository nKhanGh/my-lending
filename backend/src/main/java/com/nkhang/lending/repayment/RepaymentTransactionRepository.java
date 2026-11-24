package com.nkhang.lending.repayment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepaymentTransactionRepository extends JpaRepository<RepaymentTransaction, UUID> {
    Optional<RepaymentTransaction> findByUserIdAndIdempotencyKey(UUID userId, String key);
    boolean existsByInstalmentIdAndStatus(UUID instalmentId, RepaymentTransaction.Status status);
    boolean existsByProviderEventId(String eventId);
    List<RepaymentTransaction> findByInstalmentIdInOrderByRequestedAt(List<UUID> instalmentIds);
    List<RepaymentTransaction> findTop100ByStatusAndRequestedAtBeforeOrderByRequestedAt(
            RepaymentTransaction.Status status, Instant cutoff);
}
