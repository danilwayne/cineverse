package com.cineverse.gamification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Motor de engajamento: XP, níveis, streaks e conquistas.
 * Filosofia: recompensar uso genuíno (estilo Duolingo), sem dark patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final GamificationProfileRepository profileRepository;
    private final AchievementRepository achievementRepository;
    private final ProfileAchievementRepository earnedRepository;

    public enum GameEvent {
        REVIEW(20), ACTIVITY_UPDATE(10), WATCHLIST_ADD(5), LIST_CREATED(15);
        final int xp;
        GameEvent(int xp) { this.xp = xp; }
    }

    @Transactional
    public void initProfile(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            profileRepository.save(GamificationProfile.builder().profileId(profileId).build());
        }
    }

    @Transactional
    public void onEvent(Long profileId, GameEvent event) {
        GamificationProfile gp = load(profileId);
        touchStreak(gp);
        addXp(gp, event.xp);
        if (event == GameEvent.LIST_CREATED) grant(gp, "FIRST_LIST");
        profileRepository.save(gp);
    }

    @Transactional
    public void onReview(Long profileId, long totalReviews) {
        GamificationProfile gp = load(profileId);
        touchStreak(gp);
        addXp(gp, GameEvent.REVIEW.xp);
        if (totalReviews >= 1) grant(gp, "FIRST_REVIEW");
        if (totalReviews >= 10) grant(gp, "REVIEWS_10");
        profileRepository.save(gp);
    }

    @Transactional
    public void onWatched(Long profileId, long totalWatched) {
        GamificationProfile gp = load(profileId);
        touchStreak(gp);
        addXp(gp, GameEvent.ACTIVITY_UPDATE.xp);
        if (totalWatched >= 10) grant(gp, "MARATHON_10");
        profileRepository.save(gp);
    }

    public Status status(Long profileId) {
        GamificationProfile gp = load(profileId);
        List<ProfileAchievement> earned = earnedRepository.findByProfileId(profileId);
        List<Achievement> achievements = achievementRepository.findAllById(
                earned.stream().map(ProfileAchievement::getAchievementId).toList());
        return new Status(gp.getXp(), gp.getLevel(), gp.getStreakDays(),
                gp.getLongestStreak(), achievements);
    }

    /* ---------- internos ---------- */

    private GamificationProfile load(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseGet(() -> profileRepository.save(
                        GamificationProfile.builder().profileId(profileId).build()));
    }

    private void addXp(GamificationProfile gp, int xp) {
        gp.setXp(gp.getXp() + xp);
        int newLevel = 1 + gp.getXp() / 100;
        if (newLevel > gp.getLevel()) {
            gp.setLevel(newLevel);
            if (newLevel >= 5) grant(gp, "LEVEL_5");
        }
    }

    private void touchStreak(GamificationProfile gp) {
        LocalDate today = LocalDate.now();
        if (today.equals(gp.getLastActiveOn())) return;
        if (today.minusDays(1).equals(gp.getLastActiveOn())) {
            gp.setStreakDays(gp.getStreakDays() + 1);
        } else {
            gp.setStreakDays(1);
        }
        gp.setLongestStreak(Math.max(gp.getLongestStreak(), gp.getStreakDays()));
        gp.setLastActiveOn(today);
        if (gp.getStreakDays() >= 7) grant(gp, "STREAK_7");
        if (gp.getStreakDays() >= 30) grant(gp, "STREAK_30");
    }

    private void grant(GamificationProfile gp, String code) {
        achievementRepository.findByCode(code).ifPresent(achievement -> {
            if (!earnedRepository.existsByProfileIdAndAchievementId(gp.getProfileId(), achievement.getId())) {
                earnedRepository.save(ProfileAchievement.builder()
                        .profileId(gp.getProfileId())
                        .achievementId(achievement.getId())
                        .build());
                gp.setXp(gp.getXp() + achievement.getXpReward());
                log.info("Conquista {} concedida ao perfil {}", code, gp.getProfileId());
            }
        });
    }

    public record Status(int xp, int level, int streakDays, int longestStreak,
                         List<Achievement> achievements) {}
}
