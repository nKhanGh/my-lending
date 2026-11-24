package com.nkhang.lending.creditaccount;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.creditaccount.dto.CreditAccountResponse;
import com.nkhang.lending.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditAccountService {
    private final CreditAccountRepository repository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public CreditAccountResponse getAccount() {
        return CreditAccountResponse.from(requireAccount());
    }

    public CreditAccount requireAccount() {
        return repository.findByUserId(currentUser.id())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Credit account not found"));
    }

    public CreditAccount lockAccount() {
        return repository.lockByUserId(currentUser.id())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Credit account not found"));
    }
}
