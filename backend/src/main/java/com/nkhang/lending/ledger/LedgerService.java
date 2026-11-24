package com.nkhang.lending.ledger;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Clock;
import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public void drawdown(UUID userId, UUID accountId, UUID drawdownId, BigDecimal amount) {
        insert(userId, accountId, drawdownId, "DRAWDOWN", drawdownId, amount);
    }
    public void repayment(UUID userId, UUID accountId, UUID drawdownId, UUID repaymentId, BigDecimal amount) {
        insert(userId, accountId, drawdownId, "REPAYMENT", repaymentId, amount.negate());
    }
    private void insert(UUID userId, UUID accountId, UUID drawdownId, String event,
                        UUID referenceId, BigDecimal amount) {
        jdbc.update("INSERT INTO principal_movements(id,user_id,account_id,drawdown_id,event_type,reference_id,amount,occurred_at) VALUES (?,?,?,?,?,?,?,?)",
                UUID.randomUUID(), userId, accountId, drawdownId, event, referenceId, amount,
                Timestamp.from(clock.instant()));
    }
}
