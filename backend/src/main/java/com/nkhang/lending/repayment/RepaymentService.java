package com.nkhang.lending.repayment;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.common.idempotency.IdempotencyKeys;
import com.nkhang.lending.creditaccount.CreditAccountRepository;
import com.nkhang.lending.ledger.LedgerService;
import com.nkhang.lending.audit.AuditService;
import com.nkhang.lending.creditaccount.CreditAccountService;
import com.nkhang.lending.drawdown.*;
import com.nkhang.lending.repayment.dto.RepaymentHistoryResponse;
import com.nkhang.lending.repayment.dto.RepaymentResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepaymentService {
    private final CreditAccountService accountService;
    private final CreditAccountRepository accountRepository;
    private final CreditDrawdownRepository drawdowns;
    private final InstalmentRepository instalments;
    private final RepaymentTransactionRepository repayments;
    private final EntityManager entityManager;
    private final Clock clock;
    private final LedgerService ledger;
    private final AuditService audit;

    @Transactional
    public RepaymentResponse repay(UUID instalmentId, String rawKey) {
        return repay(instalmentId, rawKey, null);
    }

    @Transactional
    public RepaymentResponse repay(UUID instalmentId, String rawKey, BigDecimal requestedAmount) {
        if (requestedAmount != null && (requestedAmount.scale() > 2 || requestedAmount.signum() <= 0)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment amount must be positive with at most two decimals");
        }
        String key = IdempotencyKeys.requireValid(rawKey);
        var account = accountService.lockAccount();
        var existing = repayments.findByUserIdAndIdempotencyKey(account.getUserId(), key);
        if (existing.isPresent()) {
            var payment = existing.get();
            if (!payment.getInstalmentId().equals(instalmentId)
                    || (requestedAmount != null && payment.getAmount().compareTo(requestedAmount) != 0)) {
                throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            }
            var priorDrawdown = ownedDrawdown(payment.getInstalmentId(), account.getId());
            return RepaymentResponse.from(payment, priorDrawdown.getOutstandingPrincipal(), account.getAvailableLimit());
        }
        var instalment = instalments.findById(instalmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Instalment not found"));
        var drawdown = ownedDrawdown(instalmentId, account.getId());
        var next = instalments.findFirstByDrawdownIdAndStatusInOrderBySequenceNo(drawdown.getId(),
                List.of(Instalment.Status.UNPAID, Instalment.Status.OVERDUE));
        if (next.isEmpty() || !next.get().getId().equals(instalmentId)
                || instalment.getStatus() == Instalment.Status.PAID) {
            throw new AppException(ErrorCode.INVALID_REPAYMENT_STATE);
        }
        if (repayments.existsByInstalmentIdAndStatus(instalmentId, RepaymentTransaction.Status.PENDING)) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PENDING);
        }
        BigDecimal amount = requestedAmount == null ? instalment.getRemainingAmount() : requestedAmount;
        if (amount.compareTo(instalment.getRemainingAmount()) > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment exceeds remaining instalment amount");
        }
        var payment = repayments.save(new RepaymentTransaction(account.getUserId(), instalmentId,
                amount, Instant.now(clock), key));
        audit.record(account.getUserId(), "PAYMENT_REQUESTED", "REPAYMENT", payment.getId(),
                "amount=" + amount.toPlainString());
        return RepaymentResponse.from(payment, drawdown.getOutstandingPrincipal(), account.getAvailableLimit());
    }

    @Transactional
    public RepaymentResponse settle(UUID paymentId, RepaymentTransaction.Status outcome, String eventId) {
        if (outcome == RepaymentTransaction.Status.PENDING || eventId == null || eventId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        var candidate = repayments.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Payment not found"));
        var account = accountRepository.lockByUserId(candidate.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Credit account not found"));
        entityManager.refresh(candidate);
        var instalment = instalments.findById(candidate.getInstalmentId()).orElseThrow();
        var drawdown = drawdowns.findById(instalment.getDrawdownId()).orElseThrow();
        if (!drawdown.getAccountId().equals(account.getId())) throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        entityManager.refresh(instalment);
        entityManager.refresh(drawdown);
        if (candidate.getStatus() != RepaymentTransaction.Status.PENDING) {
            if (candidate.getStatus() != outcome || !eventId.equals(candidate.getProviderEventId())) {
                throw new AppException(ErrorCode.PAYMENT_ALREADY_RESOLVED);
            }
            return RepaymentResponse.from(candidate, drawdown.getOutstandingPrincipal(), account.getAvailableLimit());
        }
        if (repayments.existsByProviderEventId(eventId)) throw new AppException(ErrorCode.PAYMENT_ALREADY_RESOLVED);
        Instant now = Instant.now(clock);
        if (outcome == RepaymentTransaction.Status.SUCCESS) {
            if (instalment.getStatus() == Instalment.Status.PAID
                    || candidate.getAmount().compareTo(instalment.getRemainingAmount()) > 0) {
                throw new AppException(ErrorCode.PAYMENT_ALREADY_RESOLVED);
            }
            instalment.applyPayment(candidate.getAmount(), now);
            drawdown.repay(candidate.getAmount());
            account.restore(candidate.getAmount());
            ledger.repayment(account.getUserId(), account.getId(), drawdown.getId(), candidate.getId(),
                    candidate.getAmount());
        }
        candidate.resolve(outcome, eventId, now);
        audit.record(account.getUserId(), "PAYMENT_" + outcome.name(), "REPAYMENT", candidate.getId(),
                "event=" + eventId);
        return RepaymentResponse.from(candidate, drawdown.getOutstandingPrincipal(), account.getAvailableLimit());
    }

    @Transactional(readOnly = true)
    public RepaymentResponse payment(UUID paymentId) {
        var account = accountService.requireAccount();
        var payment = repayments.findById(paymentId)
                .filter(value -> value.getUserId().equals(account.getUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Payment not found"));
        var drawdown = ownedDrawdown(payment.getInstalmentId(), account.getId());
        return RepaymentResponse.from(payment, drawdown.getOutstandingPrincipal(), account.getAvailableLimit());
    }

    @Transactional(readOnly = true)
    public List<RepaymentHistoryResponse> history(UUID drawdownId) {
        var account = accountService.requireAccount();
        var drawdown = drawdowns.findByIdAndAccountId(drawdownId, account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Drawdown not found"));
        var ids = instalments.findByDrawdownIdOrderBySequenceNo(drawdown.getId()).stream().map(Instalment::getId).toList();
        return ids.isEmpty() ? List.of() : repayments.findByInstalmentIdInOrderByRequestedAt(ids).stream()
                .map(RepaymentHistoryResponse::from).toList();
    }

    private CreditDrawdown ownedDrawdown(UUID instalmentId, UUID accountId) {
        var instalment = instalments.findById(instalmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Instalment not found"));
        return drawdowns.findByIdAndAccountId(instalment.getDrawdownId(), accountId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Instalment not found"));
    }
}
