package com.nkhang.lending.repayment;

import com.nkhang.lending.common.api.ApiResponse;
import com.nkhang.lending.repayment.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RepaymentController {
    private final RepaymentService service;

    @PostMapping("/api/instalments/{id}/repay")
    public ResponseEntity<ApiResponse<RepaymentResponse>> repay(@PathVariable UUID id,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody(required = false) RepaymentRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(service.repay(id, key, request == null ? null : request.amount()), "Payment pending"));
    }
    @GetMapping("/api/payments/{id}")
    public ApiResponse<RepaymentResponse> payment(@PathVariable UUID id) {
        return ApiResponse.success(service.payment(id));
    }
    @PostMapping("/api/admin/payments/{id}/settle")
    public ApiResponse<RepaymentResponse> settle(@PathVariable UUID id,
            @Valid @RequestBody SettlePaymentRequest request) {
        return ApiResponse.success(service.settle(id, RepaymentTransaction.Status.valueOf(request.outcome()),
                request.providerEventId()));
    }
    @GetMapping("/api/credit-drawdowns/{id}/repayments")
    public ApiResponse<List<RepaymentHistoryResponse>> history(@PathVariable UUID id) {
        return ApiResponse.success(service.history(id));
    }
}
