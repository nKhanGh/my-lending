package com.nkhang.lending.creditaccount;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "credit_accounts")
@Getter
@NoArgsConstructor
public class CreditAccount {
    public enum Status { ACTIVE }

    @Id
    private UUID id;
    private UUID userId;
    private BigDecimal totalLimit;
    private BigDecimal usedLimit;
    private String currency;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Version
    private long version;

    public CreditAccount(UUID userId, BigDecimal totalLimit) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.totalLimit = totalLimit;
        this.usedLimit = new BigDecimal("0.00");
        this.currency = "SGD";
        this.status = Status.ACTIVE;
    }

    public BigDecimal getAvailableLimit() {
        return totalLimit.subtract(usedLimit);
    }

    public void utilize(BigDecimal amount) {
        if (status != Status.ACTIVE || amount.compareTo(getAvailableLimit()) > 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_CREDIT);
        }
        usedLimit = usedLimit.add(amount);
    }

    public void restore(BigDecimal amount) {
        if (amount.compareTo(usedLimit) > 0) {
            throw new IllegalStateException("Repayment exceeds used credit");
        }
        usedLimit = usedLimit.subtract(amount);
    }
}
