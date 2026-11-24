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
}
