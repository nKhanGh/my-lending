package com.nkhang.lending.assessment;

import com.nkhang.lending.assessment.dto.*;
import com.nkhang.lending.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credit-profile")
@RequiredArgsConstructor
public class CreditProfileController {
    private final CreditProfileService service;
    @PutMapping public ApiResponse<CreditProfileResponse> save(@Valid @RequestBody CreditProfileRequest request) {
        return ApiResponse.success(service.save(request));
    }
    @DeleteMapping public ApiResponse<Void> delete() {
        service.delete();
        return ApiResponse.success(null);
    }
    @GetMapping public ApiResponse<CreditProfileResponse> get() {
        return ApiResponse.success(service.get());
    }
}
