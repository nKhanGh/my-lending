package com.nkhang.lending.application;

import com.nkhang.lending.application.dto.CreditApplicationRequest;
import com.nkhang.lending.application.dto.CreditApplicationResponse;
import com.nkhang.lending.assessment.CreditAssessmentPolicy;
import com.nkhang.lending.assessment.CreditProfileRepository;
import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.creditaccount.CreditAccount;
import com.nkhang.lending.creditaccount.CreditAccountRepository;
import com.nkhang.lending.config.CurrentUser;
import lombok.RequiredArgsConstructor;
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
    private final CreditProfileRepository profiles;
    private final Clock clock;
    private final CurrentUser currentUser;

    @Transactional
    public CreditApplicationResponse submit(CreditApplicationRequest request) {
        if (request.currency() != null && !"SGD".equals(request.currency())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Only SGD is supported in this demo");
        }
        UUID userId = currentUser.id();
        userRepository.lockById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new AppException(ErrorCode.APPLICATION_ALREADY_APPROVED);
        }

        var surplus = request.monthlyNetIncome()
                .subtract(request.monthlyLivingExpenses())
                .subtract(request.monthlyDebtPayments());
        var profile = profiles.findById(userId);
        var explained = profile.map(value -> policy.assessV2(surplus, request.monthlyNetIncome(),
                request.monthlyDebtPayments(), value));
        var decision = explained.map(CreditAssessmentPolicy.ExplainedDecision::decision)
                .orElseGet(() -> policy.assess(surplus));
        var application = new CreditApplication(userId,
                request.monthlyNetIncome(), request.monthlyLivingExpenses(), request.monthlyDebtPayments(),
                surplus, decision.status(), decision.reasonCode(), decision.limit(), Instant.now(clock));
        if (profile.isPresent()) {
            application.recordSimulatedAssessment(new CreditApplication.CreditProfileSnapshot(
                    profile.get().getOnTimePayments(), profile.get().getMissedPayments()),
                    explained.orElseThrow().debtToIncomeRatio());
        }
        applicationRepository.save(application);
        if (decision.status() == CreditApplication.Status.APPROVED) {
            accountRepository.save(new CreditAccount(userId, decision.limit()));
        }
        return CreditApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public CreditApplicationResponse latest() {
        return applicationRepository.findFirstByUserIdOrderByAssessedAtDesc(currentUser.id())
                .map(CreditApplicationResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "No credit application yet"));
    }
}
