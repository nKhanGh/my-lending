package com.nkhang.lending.creditaccount;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CreditAccountRepository extends JpaRepository<CreditAccount, UUID> {
    Optional<CreditAccount> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from CreditAccount a where a.userId = :userId")
    Optional<CreditAccount> lockByUserId(@Param("userId") UUID userId);
}
