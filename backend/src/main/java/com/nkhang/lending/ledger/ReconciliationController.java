package com.nkhang.lending.ledger;
import com.nkhang.lending.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequiredArgsConstructor
public class ReconciliationController {
    private final ReconciliationService service;
    @GetMapping("/api/admin/reconciliation")
    public ApiResponse<List<ReconciliationResponse>> all() {
        return ApiResponse.success(service.all());
    }
}
