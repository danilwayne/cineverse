package com.cineverse.catalog.infrastructure;

import com.cineverse.catalog.domain.WatchProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchProviderRepository extends JpaRepository<WatchProvider, Long> {
    List<WatchProvider> findByTitleIdAndCountry(Long titleId, String country);
    void deleteByTitleId(Long titleId);
}
