package com.nkhang.lending.config;

import com.nkhang.lending.application.DemoUser;
import com.nkhang.lending.application.DemoUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final DemoUserRepository users;
    private final PasswordEncoder encoder;
    @Value("${lending.admin-email:}") private String email;
    @Value("${lending.admin-password:}") private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 8) {
            throw new IllegalStateException("ADMIN_EMAIL and ADMIN_PASSWORD (at least 8 characters) must both be configured");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(normalized).isEmpty()) {
            users.save(DemoUser.admin("Simulator Admin", normalized, encoder.encode(password)));
        }
    }
}
