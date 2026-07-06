package com.cineverse.watchlist.infrastructure;

import com.cineverse.watchlist.domain.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByProfileId(Long profileId);
    Optional<Watchlist> findByShareSlug(String slug);
    Optional<Watchlist> findByProfileIdAndIsDefaultTrue(Long profileId);
}
