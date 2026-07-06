package com.cineverse.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Optional<Activity> findByProfileIdAndTitleId(Long profileId, Long titleId);
    List<Activity> findByProfileIdOrderByUpdatedAtDesc(Long profileId);
    long countByProfileIdAndStatus(Long profileId, Activity.Status status);
}
