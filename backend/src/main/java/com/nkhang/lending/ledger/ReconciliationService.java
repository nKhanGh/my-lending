package com.nkhang.lending.ledger;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationService {
    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> all() {
        return jdbc.query("""
            SELECT a.id, a.user_id, a.used_limit,
              COALESCE((SELECT SUM(d.outstanding_principal) FROM credit_drawdowns d WHERE d.account_id=a.id), 0) drawdown_total,
              COALESCE((SELECT SUM(i.remaining_amount) FROM instalments i
                JOIN credit_drawdowns d ON d.id=i.drawdown_id WHERE d.account_id=a.id), 0) instalment_total,
              COALESCE((SELECT SUM(m.amount) FROM principal_movements m WHERE m.account_id=a.id), 0) ledger_total
            FROM credit_accounts a ORDER BY a.id
            """, (rs, row) -> {
            var used = rs.getBigDecimal("used_limit");
            var drawdowns = rs.getBigDecimal("drawdown_total");
            var instalments = rs.getBigDecimal("instalment_total");
            var ledger = rs.getBigDecimal("ledger_total");
            boolean matched = used.compareTo(drawdowns) == 0 && used.compareTo(instalments) == 0
                    && used.compareTo(ledger) == 0;
            return new ReconciliationResponse(rs.getObject("id", java.util.UUID.class),
                    rs.getObject("user_id", java.util.UUID.class), used, drawdowns, instalments, ledger, matched);
        });
    }
}
