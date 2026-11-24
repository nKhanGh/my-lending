package com.nkhang.lending.audit;
import com.nkhang.lending.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;
@RestController
@RequiredArgsConstructor
public class AuditController {
    private final AuditService service;
    @GetMapping("/api/audit-events")
    public ApiResponse<List<AuditEventResponse>> own() { return ApiResponse.success(service.own()); }
    @GetMapping("/api/admin/users/{id}/audit-events")
    public ApiResponse<List<AuditEventResponse>> forUser(@PathVariable UUID id) {
        return ApiResponse.success(service.forUser(id));
    }
}
