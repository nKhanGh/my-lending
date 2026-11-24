package com.nkhang.lending.auth.dto;

import java.util.UUID;

public record AuthResponse(String accessToken, String tokenType, long expiresIn,
                           UUID userId, String displayName, String role) {}
