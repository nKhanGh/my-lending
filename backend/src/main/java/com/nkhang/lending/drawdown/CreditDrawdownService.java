package com.nkhang.lending.drawdown;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.common.idempotency.IdempotencyKeys;
import com.nkhang.lending.creditaccount.CreditAccountService;
import com.nkhang.lending.ledger.LedgerService;
import com.nkhang.lending.audit.AuditService;
import com.nkhang.lending.drawdown.dto.DrawdownDetailResponse;
import com.nkhang.lending.drawdown.dto.DrawdownRequest;
import com.nkhang.lending.drawdown.dto.DrawdownResponse;
import com.nkhang.lending.drawdown.dto.InstalmentResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditDrawdownService {
    private final CreditAccountService accountService;
    private final CreditDrawdownRepository drawdownRepository;
    private final InstalmentRepository instalmentRepository;
    private final InstalmentScheduleFactory scheduleFactory;
    private final Clock clock;
    private final LedgerService ledger;
    private final AuditService audit;
    private final EntityManager entityManager;

    @Transactional
    public DrawdownResponse utilize(DrawdownRequest request, String rawKey) {
        String key = IdempotencyKeys.requireValid(rawKey);
        var account = accountService.lockAccount();
        var existing = drawdownRepository.findByAccountIdAndIdempotencyKey(account.getId(), key);
        if (existing.isPresent()) {
            var drawdown = existing.get();
            if (drawdown.getOriginalAmount().compareTo(request.amount()) != 0) {
                throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            }
            return DrawdownResponse.from(drawdown, account.getAvailableLimit());
        }

        account.utilize(request.amount());
        var now = Instant.now(clock);
        var drawdown = drawdownRepository.save(new CreditDrawdown(account.getId(), request.amount(), key, now));
        var instalments = scheduleFactory.create(drawdown.getId(), request.amount(),
                LocalDate.ofInstant(now, clock.getZone()));
        instalmentRepository.saveAll(instalments);
        entityManager.flush();
        ledger.drawdown(account.getUserId(), account.getId(), drawdown.getId(), request.amount());
        audit.record(account.getUserId(), "DRAWDOWN_CREATED", "CREDIT_DRAWDOWN", drawdown.getId(),
                "amount=" + request.amount().toPlainString());
        return DrawdownResponse.from(drawdown, account.getAvailableLimit());
    }

    @Transactional(readOnly = true)
    public List<DrawdownResponse> list() {
        var account = accountService.requireAccount();
        return drawdownRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(drawdown -> DrawdownResponse.from(drawdown, account.getAvailableLimit()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DrawdownDetailResponse get(UUID id) {
        var account = accountService.requireAccount();
        var drawdown = drawdownRepository.findByIdAndAccountId(id, account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Drawdown not found"));
        var instalments = instalmentRepository.findByDrawdownIdOrderBySequenceNo(id).stream()
                .map(InstalmentResponse::from).toList();
        return DrawdownDetailResponse.from(drawdown, instalments);
    }
}
