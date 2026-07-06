package com.cineverse.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private String locale = "pt-BR";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Plan plan = Plan.FREE;

    private OffsetDateTime premiumUntil;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime deletedAt;

    public enum Role { USER, ADMIN }
    public enum Plan { FREE, PREMIUM }
}
