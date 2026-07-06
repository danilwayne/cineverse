package com.cineverse.gamification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface GamificationProfileRepository extends JpaRepository<GamificationProfile, Long> {}

interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByCode(String code);
}

interface ProfileAchievementRepository extends JpaRepository<ProfileAchievement, ProfileAchievement.Pk> {
    List<ProfileAchievement> findByProfileId(Long profileId);
    boolean existsByProfileIdAndAchievementId(Long profileId, Long achievementId);
}
