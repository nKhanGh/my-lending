package com.nkhang.lending.drawdown;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstalmentRepository extends JpaRepository<Instalment, UUID> {
    List<Instalment> findByDrawdownIdOrderBySequenceNo(UUID drawdownId);
    Optional<Instalment> findFirstByDrawdownIdAndStatusOrderBySequenceNo(UUID drawdownId, Instalment.Status status);
}
