package com.nkhang.lending;

import com.nkhang.lending.drawdown.Instalment;
import com.nkhang.lending.drawdown.InstalmentRepository;
import com.nkhang.lending.drawdown.OverdueJob;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstalmentLifecycleTest {
    @Test void overdueAndPartialPaymentFollowClock() {
        var due = LocalDate.of(2026, 2, 28);
        var instalment = new Instalment(UUID.randomUUID(), 1, new BigDecimal("100.00"), due);
        var repository = mock(InstalmentRepository.class);
        when(repository.findByStatusIn(anyList())).thenReturn(List.of(instalment));
        new OverdueJob(repository, Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneOffset.UTC)).update();
        assertEquals(Instalment.Status.OVERDUE, instalment.getStatus());
        assertEquals(1, instalment.getDaysPastDue());
        instalment.applyPayment(new BigDecimal("30.25"), Instant.parse("2026-03-01T01:00:00Z"));
        assertEquals(new BigDecimal("69.75"), instalment.getRemainingAmount());
        assertEquals(Instalment.Status.OVERDUE, instalment.getStatus());
        instalment.applyPayment(new BigDecimal("69.75"), Instant.parse("2026-03-03T01:00:00Z"));
        assertEquals(Instalment.Status.PAID, instalment.getStatus());
        assertEquals(0, instalment.getRemainingAmount().signum());
    }
}
