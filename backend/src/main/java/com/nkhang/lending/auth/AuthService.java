package com.nkhang.lending.auth;

import com.nkhang.lending.application.DemoUser;
import com.nkhang.lending.application.DemoUserRepository;
import com.nkhang.lending.auth.dto.*;
import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final DemoUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;
    private final CurrentUser currentUser;
    private final Clock clock;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent()) throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        try {
            DemoUser user = users.saveAndFlush(DemoUser.customer(request.displayName().trim(), email,
                    encoder.encode(request.password())));
            return token(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        DemoUser user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        if (user.getPasswordHash() == null || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        return token(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(users.findById(currentUser.id())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED)));
    }

    private AuthResponse token(DemoUser user) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("mini-lending")
                .subject(user.getId().toString()).issuedAt(now).expiresAt(now.plusSeconds(3600))
                .claim("role", user.getRole().name()).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(value, "Bearer", 3600, user.getId(), user.getDisplayName(), user.getRole().name());
    }
}
