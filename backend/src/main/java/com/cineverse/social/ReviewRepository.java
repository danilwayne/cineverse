package com.cineverse.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTitleIdAndStatusOrderByCreatedAtDesc(Long titleId, String status);
    Optional<Review> findByProfileIdAndTitleId(Long profileId, Long titleId);
    long countByProfileId(Long profileId);
}
