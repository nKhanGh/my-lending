package com.nkhang.lending.repayment;

import com.nkhang.lending.common.api.ApiResponse;
import com.nkhang.lending.repayment.dto.RepaymentHistoryResponse;
import com.nkhang.lending.repayment.dto.RepaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RepaymentController {
    private final RepaymentService service;

    @PostMapping("/api/instalments/{id}/repay")
    public ApiResponse<RepaymentResponse> repay(@PathVariable UUID id,
                                                 @RequestHeader("Idempotency-Key") String key) {
        return ApiResponse.success(service.repay(id, key));
    }

    @GetMapping("/api/credit-drawdowns/{id}/repayments")
    public ApiResponse<List<RepaymentHistoryResponse>> history(@PathVariable UUID id) {
        return ApiResponse.success(service.history(id));
    }
}
