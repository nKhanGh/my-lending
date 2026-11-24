package com.nkhang.lending.creditaccount;

import com.nkhang.lending.common.api.ApiResponse;
import com.nkhang.lending.creditaccount.dto.CreditAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreditAccountController {
    private final CreditAccountService service;

    @GetMapping("/api/credit-account")
    public ApiResponse<CreditAccountResponse> getAccount() {
        return ApiResponse.success(service.getAccount());
    }
}
