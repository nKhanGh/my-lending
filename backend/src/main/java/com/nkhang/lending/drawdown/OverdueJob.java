package com.nkhang.lending.drawdown;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueJob {
    private final InstalmentRepository instalments;
    private final Clock clock;

    @Transactional
    @Scheduled(cron = "${lending.overdue-cron:0 5 0 * * *}", zone = "UTC")
    public void update() {
        var today = LocalDate.now(clock);
        instalments.findByStatusIn(List.of(Instalment.Status.UNPAID, Instalment.Status.OVERDUE))
                .forEach(value -> value.updateOverdue(today));
    }
}
