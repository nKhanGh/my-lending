package com.nkhang.lending.application;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class DemoUser {
    @Id
    private UUID id;
    private String displayName;
    private String email;
    private String passwordHash;
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private UserRole role;

    public static DemoUser customer(String displayName, String email, String passwordHash) {
        DemoUser user = new DemoUser();
        user.id = UUID.randomUUID();
        user.displayName = displayName;
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = UserRole.CUSTOMER;
        return user;
    }

    public static DemoUser admin(String displayName, String email, String passwordHash) {
        DemoUser user = customer(displayName, email, passwordHash);
        user.role = UserRole.ADMIN;
        return user;
    }

    public void setDemoCredentials(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
