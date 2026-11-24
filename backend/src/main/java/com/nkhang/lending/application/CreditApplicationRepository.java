package com.nkhang.lending.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditApplicationRepository extends JpaRepository<CreditApplication, UUID> {
    Optional<CreditApplication> findFirstByUserIdOrderByAssessedAtDesc(UUID userId);
}
