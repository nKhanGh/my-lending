package com.nkhang.lending.repayment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepaymentTransactionRepository extends JpaRepository<RepaymentTransaction, UUID> {
    Optional<RepaymentTransaction> findByIdempotencyKey(String idempotencyKey);
    List<RepaymentTransaction> findByInstalmentIdInOrderByPaidAt(List<UUID> instalmentIds);
}
