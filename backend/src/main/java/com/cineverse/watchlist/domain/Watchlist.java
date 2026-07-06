package com.cineverse.watchlist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "watchlists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Watchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private boolean isDefault = false;

    /** null = privada; preenchido = compartilhável por link */
    @Column(name = "share_slug", unique = true)
    private String shareSlug;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
