package com.nkhang.lending.auth;

import com.nkhang.lending.auth.dto.*;
import com.nkhang.lending.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(service.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(service.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() { return ApiResponse.success(service.me()); }
}
