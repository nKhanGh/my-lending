package com.nkhang.lending.application;

import com.nkhang.lending.application.dto.CreditApplicationRequest;
import com.nkhang.lending.application.dto.CreditApplicationResponse;
import com.nkhang.lending.assessment.CreditAssessmentPolicy;
import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.creditaccount.CreditAccount;
import com.nkhang.lending.creditaccount.CreditAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditApplicationService {
    private final DemoUserRepository userRepository;
    private final CreditApplicationRepository applicationRepository;
    private final CreditAccountRepository accountRepository;
    private final CreditAssessmentPolicy policy;
    private final Clock clock;

    @Value("${lending.demo-user-id}")
    private UUID demoUserId;

    @Transactional
    public CreditApplicationResponse submit(CreditApplicationRequest request) {
        if (request.currency() != null && !"SGD".equals(request.currency())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Only SGD is supported in this demo");
        }
        userRepository.lockById(demoUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Demo user not found"));
        if (accountRepository.findByUserId(demoUserId).isPresent()) {
            throw new AppException(ErrorCode.APPLICATION_ALREADY_APPROVED);
        }

        var surplus = request.monthlyNetIncome()
                .subtract(request.monthlyLivingExpenses())
                .subtract(request.monthlyDebtPayments());
        var decision = policy.assess(surplus);
        var application = applicationRepository.save(new CreditApplication(demoUserId,
                request.monthlyNetIncome(), request.monthlyLivingExpenses(), request.monthlyDebtPayments(),
                surplus, decision.status(), decision.reasonCode(), decision.limit(), Instant.now(clock)));
        if (decision.status() == CreditApplication.Status.APPROVED) {
            accountRepository.save(new CreditAccount(demoUserId, decision.limit()));
        }
        return CreditApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public CreditApplicationResponse latest() {
        return applicationRepository.findFirstByUserIdOrderByAssessedAtDesc(demoUserId)
                .map(CreditApplicationResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "No credit application yet"));
    }
}
