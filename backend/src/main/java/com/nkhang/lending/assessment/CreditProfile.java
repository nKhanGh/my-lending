package com.nkhang.lending.assessment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_profiles")
@Getter
@NoArgsConstructor
public class CreditProfile {
    @Id private UUID userId;
    private int onTimePayments;
    private int missedPayments;
    private Instant updatedAt;

    public CreditProfile(UUID userId, int onTimePayments, int missedPayments, Instant updatedAt) {
        this.userId = userId;
        update(onTimePayments, missedPayments, updatedAt);
    }
    public void update(int onTimePayments, int missedPayments, Instant at) {
        this.onTimePayments = onTimePayments;
        this.missedPayments = missedPayments;
        updatedAt = at;
    }
}
