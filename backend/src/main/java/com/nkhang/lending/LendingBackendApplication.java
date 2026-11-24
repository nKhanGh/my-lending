package com.nkhang.lending;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class LendingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LendingBackendApplication.class, args);
    }
}

