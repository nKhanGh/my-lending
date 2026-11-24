package com.nkhang.lending.creditaccount;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.creditaccount.dto.CreditAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditAccountService {
    private final CreditAccountRepository repository;

    @Value("${lending.demo-user-id}")
    private UUID demoUserId;

    @Transactional(readOnly = true)
    public CreditAccountResponse getAccount() {
        return CreditAccountResponse.from(requireAccount());
    }

    public CreditAccount requireAccount() {
        return repository.findByUserId(demoUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Credit account not found"));
    }

    public CreditAccount lockAccount() {
        return repository.lockByUserId(demoUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Credit account not found"));
    }
}
