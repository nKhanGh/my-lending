package com.nkhang.lending.repayment;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.common.idempotency.IdempotencyKeys;
import com.nkhang.lending.creditaccount.CreditAccountService;
import com.nkhang.lending.drawdown.CreditDrawdown;
import com.nkhang.lending.drawdown.CreditDrawdownRepository;
import com.nkhang.lending.drawdown.Instalment;
import com.nkhang.lending.drawdown.InstalmentRepository;
import com.nkhang.lending.repayment.dto.RepaymentHistoryResponse;
import com.nkhang.lending.repayment.dto.RepaymentResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepaymentService {
    private final CreditAccountService accountService;
    private final CreditDrawdownRepository drawdownRepository;
    private final InstalmentRepository instalmentRepository;
    private final RepaymentTransactionRepository repaymentRepository;
    private final EntityManager entityManager;
    private final Clock clock;

    @Transactional
    public RepaymentResponse repay(UUID instalmentId, String rawKey) {
        String key = IdempotencyKeys.requireValid(rawKey);
        var instalment = instalmentRepository.findById(instalmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Instalment not found"));
        var drawdown = drawdownRepository.findById(instalment.getDrawdownId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Drawdown not found"));
        var account = accountService.lockAccount();
        if (!drawdown.getAccountId().equals(account.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Instalment not found");
        }

        // Both entities may have been read before waiting for the account row lock.
        entityManager.refresh(instalment);
        entityManager.refresh(drawdown);
        var existing = repaymentRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            var repayment = existing.get();
            if (!repayment.getInstalmentId().equals(instalmentId)) {
                throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            }
            return RepaymentResponse.from(repayment, drawdown.getOutstandingPrincipal(), account.getAvailableLimit());
        }

        var next = instalmentRepository.findFirstByDrawdownIdAndStatusOrderBySequenceNo(
                drawdown.getId(), Instalment.Status.UNPAID);
        if (next.isEmpty() || !next.get().getId().equals(instalmentId)
                || instalment.getStatus() != Instalment.Status.UNPAID) {
            throw new AppException(ErrorCode.INVALID_REPAYMENT_STATE);
        }

        Instant now = Instant.now(clock);
        var repayment = repaymentRepository.save(new RepaymentTransaction(instalmentId,
                instalment.getAmount(), now, key));
        instalment.markPaid(now);
        drawdown.repay(instalment.getAmount());
        account.restore(instalment.getAmount());
        return RepaymentResponse.from(repayment, drawdown.getOutstandingPrincipal(), account.getAvailableLimit());
    }

    @Transactional(readOnly = true)
    public List<RepaymentHistoryResponse> history(UUID drawdownId) {
        var account = accountService.requireAccount();
        CreditDrawdown drawdown = drawdownRepository.findByIdAndAccountId(drawdownId, account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Drawdown not found"));
        var ids = instalmentRepository.findByDrawdownIdOrderBySequenceNo(drawdown.getId()).stream()
                .map(Instalment::getId).toList();
        return repaymentRepository.findByInstalmentIdInOrderByPaidAt(ids).stream()
                .map(RepaymentHistoryResponse::from).toList();
    }
}
