package com.cineverse.social;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "title_id", nullable = false)
    private Long titleId;

    @Column(nullable = false)
    private Short rating;              // 1 a 10

    @Column(columnDefinition = "text")
    private String body;

    @Builder.Default
    private boolean spoiler = false;

    @Builder.Default
    private String status = "PUBLISHED";   // PUBLISHED | HIDDEN (moderação)

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
