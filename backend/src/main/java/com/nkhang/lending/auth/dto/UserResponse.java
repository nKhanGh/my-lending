package com.nkhang.lending.auth.dto;

import com.nkhang.lending.application.DemoUser;
import java.util.UUID;

public record UserResponse(UUID id, String displayName, String email, String role) {
    public static UserResponse from(DemoUser user) {
        return new UserResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRole().name());
    }
}
