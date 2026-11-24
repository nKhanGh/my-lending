package com.nkhang.lending;

import com.nkhang.lending.drawdown.InstalmentScheduleFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstalmentScheduleFactoryTest {
    private final InstalmentScheduleFactory factory = new InstalmentScheduleFactory();

    @Test
    void allocatesRemainderToLastMonthlyInstalment() {
        var schedule = factory.create(UUID.randomUUID(), new BigDecimal("100.01"), LocalDate.of(2026, 1, 31));
        assertEquals(4, schedule.size());
        assertEquals(new BigDecimal("25.00"), schedule.get(0).getAmount());
        assertEquals(new BigDecimal("25.00"), schedule.get(1).getAmount());
        assertEquals(new BigDecimal("25.00"), schedule.get(2).getAmount());
        assertEquals(new BigDecimal("25.01"), schedule.get(3).getAmount());
        assertEquals(LocalDate.of(2026, 2, 28), schedule.get(0).getDueDate());
        assertEquals(LocalDate.of(2026, 5, 31), schedule.get(3).getDueDate());
        assertEquals(new BigDecimal("100.01"), schedule.stream().map(i -> i.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
