package com.cineverse.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "watch_providers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_id", nullable = false)
    private Long titleId;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String kind;          // stream | rent | buy | cinema

    private String url;

    @Builder.Default
    private String country = "BR";
}
