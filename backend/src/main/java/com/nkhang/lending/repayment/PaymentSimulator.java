package com.nkhang.lending.repayment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PaymentSimulator {
    private final RepaymentService service;
    private final RepaymentTransactionRepository repository;
    private final Clock clock;
    @Value("${lending.payment-delay-seconds:5}") private long delaySeconds;
    @Value("${lending.payment-failure-percent:10}") private int failurePercent;

    @Scheduled(fixedDelayString = "${lending.payment-poll-ms:1000}")
    public void process() {
        if (delaySeconds < 0) return;
        var cutoff = clock.instant().minus(Duration.ofSeconds(delaySeconds));
        for (var payment : repository.findTop100ByStatusAndRequestedAtBeforeOrderByRequestedAt(
                RepaymentTransaction.Status.PENDING, cutoff)) {
            boolean failed = Math.floorMod(payment.getId().hashCode(), 100) < failurePercent;
            try {
                service.settle(payment.getId(), failed ? RepaymentTransaction.Status.FAILED
                        : RepaymentTransaction.Status.SUCCESS, "sim-" + payment.getId());
            } catch (RuntimeException ignored) {
                // A concurrent settlement may have resolved this attempt already.
            }
        }
    }
}
