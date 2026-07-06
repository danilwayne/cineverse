package com.cineverse.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "titles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Title {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Column(name = "media_type", nullable = false)
    private String mediaType;                 // movie | tv

    @Column(nullable = false)
    private String name;

    private String originalName;

    @Column(columnDefinition = "text")
    private String overview;

    private String posterPath;
    private String backdropPath;
    private LocalDate releaseDate;
    private Integer runtimeMin;
    private BigDecimal voteAverage;

    /** Hibernate 6 mapeia String[] -> text[] no Postgres */
    private String[] genres;
    private String[] languages;

    // A coluna "embedding vector(768)" existe no banco (pgvector) e é
    // preenchida/consultada via SQL nativo — não mapeada pela JPA.

    @Builder.Default
    private OffsetDateTime syncedAt = OffsetDateTime.now();
}
