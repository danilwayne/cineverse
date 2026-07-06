package com.cineverse.watchlist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "watchlist_items")
@IdClass(WatchlistItem.Pk.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchlistItem {

    @Id
    @Column(name = "watchlist_id")
    private Long watchlistId;

    @Id
    @Column(name = "title_id")
    private Long titleId;

    @Column(name = "added_by")
    private Long addedBy;

    @Builder.Default
    private OffsetDateTime addedAt = OffsetDateTime.now();

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long watchlistId;
        private Long titleId;
    }
}
