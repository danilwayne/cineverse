package com.cineverse.watchlist.infrastructure;

import com.cineverse.watchlist.domain.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, WatchlistItem.Pk> {
    List<WatchlistItem> findByWatchlistId(Long watchlistId);
    void deleteByWatchlistIdAndTitleId(Long watchlistId, Long titleId);
}
