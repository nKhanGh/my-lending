package com.nkhang.lending;

import com.nkhang.lending.application.CreditApplicationRepository;
import com.nkhang.lending.application.CreditApplicationService;
import com.nkhang.lending.application.dto.CreditApplicationRequest;
import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.creditaccount.CreditAccountRepository;
import com.nkhang.lending.creditaccount.CreditAccountService;
import com.nkhang.lending.drawdown.CreditDrawdown;
import com.nkhang.lending.drawdown.CreditDrawdownRepository;
import com.nkhang.lending.drawdown.CreditDrawdownService;
import com.nkhang.lending.drawdown.InstalmentRepository;
import com.nkhang.lending.drawdown.dto.DrawdownRequest;
import com.nkhang.lending.repayment.RepaymentService;
import com.nkhang.lending.repayment.RepaymentTransaction;
import com.nkhang.lending.repayment.RepaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CreditLineFlowIntegrationTest extends PostgresIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired CreditApplicationService applicationService;
    @Autowired CreditApplicationRepository applicationRepository;
    @Autowired CreditAccountService accountService;
    @Autowired CreditAccountRepository accountRepository;
    @Autowired CreditDrawdownService drawdownService;
    @Autowired CreditDrawdownRepository drawdownRepository;
    @Autowired InstalmentRepository instalmentRepository;
    @Autowired RepaymentService repaymentService;
    @Autowired RepaymentTransactionRepository repaymentRepository;
    @Autowired JdbcTemplate jdbc;

    private static final String DEMO_ID = "00000000-0000-0000-0000-000000000001";

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(DEMO_ID, null, java.util.List.of()));
    }

    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    @BeforeEach
    void reset() {
        authenticate();
        jdbc.execute("TRUNCATE TABLE credit_applications, credit_accounts CASCADE");
    }

    @Test
    void httpAssessmentAndAccountContract() throws Exception {
        mockMvc.perform(post("/api/credit-applications").with(jwt().jwt(token -> token.subject(DEMO_ID)))
                        .contentType("application/json")
                        .content("{\"monthlyNetIncome\":-1,\"monthlyLivingExpenses\":0,\"monthlyDebtPayments\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/credit-applications").with(jwt().jwt(token -> token.subject(DEMO_ID)))
                        .contentType("application/json")
                        .content("{\"monthlyNetIncome\":3000,\"monthlyLivingExpenses\":1400,\"monthlyDebtPayments\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.decision").value("APPROVED"))
                .andExpect(jsonPath("$.result.approvedLimit").value(1000));
        mockMvc.perform(get("/api/credit-account").with(jwt().jwt(token -> token.subject(DEMO_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.availableLimit").value(1000));
    }

    @Test
    void rejectedApplicationDoesNotCreateAccountAndCanBeResubmitted() {
        var rejected = applicationService.submit(new CreditApplicationRequest(
                money("799.99"), money("400.00"), money("0.00"), null));
        assertEquals("INSUFFICIENT_SURPLUS", rejected.reasonCode());
        assertEquals(0, accountRepository.count());
        var approved = approve();
        assertEquals("APPROVED_TIER_1000", approved.reasonCode());
        assertEquals(1, accountRepository.count());
        assertEquals(approved.applicationId(), applicationService.latest().applicationId());
        assertEquals(ErrorCode.APPLICATION_ALREADY_APPROVED,
                assertThrows(AppException.class, this::approve).getErrorCode());
    }

    @Test
    void drawdownRejectsOverLimitAndRetryDoesNotDuplicate() {
        approve();
        assertEquals(ErrorCode.INSUFFICIENT_CREDIT,
                assertThrows(AppException.class, () -> drawdownService.utilize(
                        new DrawdownRequest(money("1200.00")), "too-large")).getErrorCode());
        assertMoney("0.00", accountService.getAccount().usedLimit());

        var first = drawdownService.utilize(new DrawdownRequest(money("800.00")), "draw-1");
        var retry = drawdownService.utilize(new DrawdownRequest(money("800.00")), "draw-1");
        assertEquals(first.drawdownId(), retry.drawdownId());
        assertEquals(1, drawdownRepository.count());
        assertEquals(4, instalmentRepository.count());
        assertMoney("800.00", accountService.getAccount().usedLimit());
        assertMoney("200.00", accountService.getAccount().availableLimit());
        assertEquals(ErrorCode.IDEMPOTENCY_KEY_REUSED,
                assertThrows(AppException.class, () -> drawdownService.utilize(
                        new DrawdownRequest(money("700.00")), "draw-1")).getErrorCode());
    }

    @Test
    void repaymentIsOrderedIdempotentAndClosesDrawdown() {
        approve();
        var drawdown = drawdownService.utilize(new DrawdownRequest(money("800.00")), "draw-1");
        var schedule = drawdownService.get(drawdown.drawdownId()).instalments();
        assertEquals(ErrorCode.INVALID_REPAYMENT_STATE,
                assertThrows(AppException.class, () -> repaymentService.repay(schedule.get(1).id(), "repay-2"))
                        .getErrorCode());
        assertMoney("800.00", accountService.getAccount().usedLimit());

        var first = repaymentService.repay(schedule.get(0).id(), "repay-1");
        var retry = repaymentService.repay(schedule.get(0).id(), "repay-1");
        assertEquals(first.repaymentId(), retry.repaymentId());
        assertEquals(RepaymentTransaction.Status.PENDING, first.status());
        assertMoney("800.00", first.remainingDrawdownPrincipal());
        assertMoney("200.00", first.availableLimit());
        assertEquals(1, repaymentRepository.count());
        assertEquals(ErrorCode.PAYMENT_ALREADY_PENDING,
                assertThrows(AppException.class, () -> repaymentService.repay(schedule.get(0).id(), "another-key"))
                        .getErrorCode());
        assertEquals(ErrorCode.IDEMPOTENCY_KEY_REUSED,
                assertThrows(AppException.class, () -> repaymentService.repay(schedule.get(1).id(), "repay-1"))
                        .getErrorCode());

        var settled = repaymentService.settle(first.repaymentId(), RepaymentTransaction.Status.SUCCESS, "provider-1");
        assertMoney("600.00", settled.remainingDrawdownPrincipal());
        assertMoney("400.00", settled.availableLimit());
        var duplicate = repaymentService.settle(first.repaymentId(), RepaymentTransaction.Status.SUCCESS, "provider-1");
        assertMoney("600.00", duplicate.remainingDrawdownPrincipal());
        assertEquals(ErrorCode.PAYMENT_ALREADY_RESOLVED,
                assertThrows(AppException.class, () -> repaymentService.settle(first.repaymentId(),
                        RepaymentTransaction.Status.FAILED, "late-failure")).getErrorCode());
        for (int i = 1; i < 4; i++) {
            var attempt = repaymentService.repay(schedule.get(i).id(), "repay-" + (i + 1));
            repaymentService.settle(attempt.repaymentId(), RepaymentTransaction.Status.SUCCESS, "provider-" + (i + 1));
        }
        var closed = drawdownRepository.findById(drawdown.drawdownId()).orElseThrow();
        assertEquals(CreditDrawdown.Status.PAID_OFF, closed.getStatus());
        assertMoney("0.00", closed.getOutstandingPrincipal());
        assertMoney("0.00", accountService.getAccount().usedLimit());
        assertMoney("1000.00", accountService.getAccount().availableLimit());
        assertEquals(4, repaymentService.history(drawdown.drawdownId()).size());
    }

    @Test
    void failedPaymentDoesNotChangeBalanceAndCanBeRetried() {
        approve();
        var drawdown = drawdownService.utilize(new DrawdownRequest(money("400.00")), "draw-fail");
        var instalment = drawdownService.get(drawdown.drawdownId()).instalments().get(0);
        var attempt = repaymentService.repay(instalment.id(), "failed-first");
        var failed = repaymentService.settle(attempt.repaymentId(), RepaymentTransaction.Status.FAILED, "provider-failed");
        assertEquals(RepaymentTransaction.Status.FAILED, failed.status());
        assertMoney("400.00", accountService.getAccount().usedLimit());
        var retry = repaymentService.repay(instalment.id(), "new-key-after-failure");
        assertNotEquals(attempt.repaymentId(), retry.repaymentId());
        repaymentService.settle(retry.repaymentId(), RepaymentTransaction.Status.SUCCESS, "provider-success");
        assertMoney("300.00", accountService.getAccount().usedLimit());
    }

    @Test
    void invalidScheduleRollsBackAccountUtilization() {
        approve();
        assertThrows(AppException.class, () -> drawdownService.utilize(
                new DrawdownRequest(money("0.01")), "invalid"));
        assertMoney("0.00", accountService.getAccount().usedLimit());
        assertEquals(0, drawdownRepository.count());
    }

    @Test
    void concurrentDrawdownsCannotExceedLimit() throws Exception {
        approve();
        var start = new CountDownLatch(1);
        var ready = new CountDownLatch(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> attemptDrawdown("concurrent-1", ready, start));
            var second = executor.submit(() -> attemptDrawdown("concurrent-2", ready, start));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
        assertEquals(1, drawdownRepository.count());
        assertMoney("700.00", accountService.getAccount().usedLimit());
        assertMoney("300.00", accountService.getAccount().availableLimit());
    }

    private boolean attemptDrawdown(String key, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        try {
            authenticate();
            drawdownService.utilize(new DrawdownRequest(money("700.00")), key);
            return true;
        } catch (AppException exception) {
            if (exception.getErrorCode() == ErrorCode.INSUFFICIENT_CREDIT) {
                return false;
            }
            throw exception;
        }
    }

    private com.nkhang.lending.application.dto.CreditApplicationResponse approve() {
        return applicationService.submit(new CreditApplicationRequest(
                money("3000.00"), money("1400.00"), money("500.00"), null));
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, money(expected).compareTo(actual));
    }
}
