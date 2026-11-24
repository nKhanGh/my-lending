package com.nkhang.lending.application;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DemoUserRepository extends JpaRepository<DemoUser, UUID> {
    Optional<DemoUser> findByEmailIgnoreCase(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from DemoUser u where u.id = :id")
    Optional<DemoUser> lockById(@Param("id") UUID id);
}
