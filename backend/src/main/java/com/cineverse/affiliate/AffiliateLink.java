package com.cineverse.affiliate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "affiliate_links")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AffiliateLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String network;

    @Column(name = "tracked_url", nullable = false)
    private String trackedUrl;

    @Builder.Default
    private boolean active = true;
}
