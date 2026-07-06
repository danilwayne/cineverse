package com.cineverse.activity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "title_id", nullable = false)
    private Long titleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private Integer season;
    private Integer episode;

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public enum Status { WATCHING, WATCHED, DROPPED, PLANNED }
}
