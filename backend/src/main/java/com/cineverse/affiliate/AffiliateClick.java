package com.cineverse.affiliate;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "affiliate_clicks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AffiliateClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Column(name = "profile_id")
    private Long profileId;

    @Builder.Default
    private OffsetDateTime clickedAt = OffsetDateTime.now();
}
