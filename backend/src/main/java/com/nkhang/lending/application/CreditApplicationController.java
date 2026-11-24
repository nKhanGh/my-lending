package com.nkhang.lending.application;

import com.nkhang.lending.application.dto.CreditApplicationRequest;
import com.nkhang.lending.application.dto.CreditApplicationResponse;
import com.nkhang.lending.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credit-applications")
@RequiredArgsConstructor
public class CreditApplicationController {
    private final CreditApplicationService service;

    @PostMapping
    public ApiResponse<CreditApplicationResponse> submit(@Valid @RequestBody CreditApplicationRequest request) {
        return ApiResponse.success(service.submit(request));
    }

    @GetMapping("/latest")
    public ApiResponse<CreditApplicationResponse> latest() {
        return ApiResponse.success(service.latest());
    }
}
