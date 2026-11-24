package com.nkhang.lending.config;

import com.nkhang.lending.application.DemoUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DemoCredentialsBootstrap implements ApplicationRunner {
    private final DemoUserRepository users;
    private final PasswordEncoder encoder;

    @Value("${lending.demo-user-id}") private UUID demoId;
    @Value("${lending.demo-password:DemoPass123!}") private String demoPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var demo = users.findById(demoId).orElseThrow();
        if (demo.getPasswordHash() == null) {
            demo.setDemoCredentials("demo@local.test", encoder.encode(demoPassword));
        }
    }
}
