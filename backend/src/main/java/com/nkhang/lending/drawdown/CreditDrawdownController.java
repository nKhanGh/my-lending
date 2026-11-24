package com.nkhang.lending.drawdown;

import com.nkhang.lending.common.api.ApiResponse;
import com.nkhang.lending.drawdown.dto.DrawdownDetailResponse;
import com.nkhang.lending.drawdown.dto.DrawdownRequest;
import com.nkhang.lending.drawdown.dto.DrawdownResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/credit-drawdowns")
@RequiredArgsConstructor
public class CreditDrawdownController {
    private final CreditDrawdownService service;

    @PostMapping
    public ApiResponse<DrawdownResponse> utilize(@Valid @RequestBody DrawdownRequest request,
                                                  @RequestHeader("Idempotency-Key") String key) {
        return ApiResponse.success(service.utilize(request, key));
    }

    @GetMapping
    public ApiResponse<List<DrawdownResponse>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<DrawdownDetailResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(service.get(id));
    }
}
