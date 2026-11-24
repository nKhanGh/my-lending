package com.nkhang.lending.creditaccount.dto;

import com.nkhang.lending.creditaccount.CreditAccount;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditAccountResponse(UUID accountId, BigDecimal totalLimit, BigDecimal usedLimit,
                                    BigDecimal availableLimit, String currency, CreditAccount.Status status) {
    public static CreditAccountResponse from(CreditAccount account) {
        return new CreditAccountResponse(account.getId(), account.getTotalLimit(), account.getUsedLimit(),
                account.getAvailableLimit(), account.getCurrency(), account.getStatus());
    }
}
