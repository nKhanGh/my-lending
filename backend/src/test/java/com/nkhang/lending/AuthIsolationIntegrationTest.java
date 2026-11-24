package com.nkhang.lending;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkhang.lending.auth.AuthService;
import com.nkhang.lending.auth.dto.RegisterRequest;
import com.nkhang.lending.creditaccount.CreditAccountRepository;
import com.nkhang.lending.drawdown.CreditDrawdownRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthIsolationIntegrationTest extends PostgresIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    @Autowired ObjectMapper mapper;
    @Autowired CreditAccountRepository accounts;
    @Autowired CreditDrawdownRepository drawdowns;

    @Test
    void accountsCannotReadOrPayEachOthersDebt() throws Exception {
        var first = auth.register(new RegisterRequest("First", "first-" + java.util.UUID.randomUUID() + "@test.local", "Password123!"));
        var second = auth.register(new RegisterRequest("Second", "second-" + java.util.UUID.randomUUID() + "@test.local", "Password123!"));
        String firstToken = "Bearer " + first.accessToken();
        String secondToken = "Bearer " + second.accessToken();
        String assessment = "{\"monthlyNetIncome\":3000,\"monthlyLivingExpenses\":1400,\"monthlyDebtPayments\":500}";
        mvc.perform(post("/api/credit-applications").header("Authorization", firstToken)
                .contentType(MediaType.APPLICATION_JSON).content(assessment)).andExpect(status().isOk());
        mvc.perform(get("/api/credit-account").header("Authorization", secondToken)).andExpect(status().isNotFound());
        mvc.perform(post("/api/credit-applications").header("Authorization", secondToken)
                .contentType(MediaType.APPLICATION_JSON).content(assessment)).andExpect(status().isOk());
        mvc.perform(get("/api/credit-account").header("Authorization", firstToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.usedLimit").value(0));
        String result = mvc.perform(post("/api/credit-drawdowns").header("Authorization", firstToken)
                .header("Idempotency-Key", "cross-account-draw")
                .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":400}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String drawdownId = mapper.readTree(result).path("result").path("drawdownId").asText();
        String detail = mvc.perform(get("/api/credit-drawdowns/" + drawdownId).header("Authorization", firstToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String instalmentId = mapper.readTree(detail).path("result").path("instalments").get(0).path("id").asText();
        mvc.perform(get("/api/credit-drawdowns/" + drawdownId).header("Authorization", secondToken))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/instalments/" + instalmentId + "/repay").header("Authorization", secondToken)
                .header("Idempotency-Key", "cross-account-repay")).andExpect(status().isNotFound());
        mvc.perform(get("/api/credit-account")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/reconciliation").header("Authorization", firstToken))
                .andExpect(status().isForbidden());
    }
}
