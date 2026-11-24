package com.nkhang.lending.assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CreditProfileRepository extends JpaRepository<CreditProfile, UUID> {}
