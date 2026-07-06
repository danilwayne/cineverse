package com.cineverse.gamification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gamification_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GamificationProfile {
    @Id
    @Column(name = "profile_id")
    private Long profileId;

    @Builder.Default
    private int xp = 0;

    @Builder.Default
    private int level = 1;

    @Builder.Default
    private int streakDays = 0;

    @Builder.Default
    private int longestStreak = 0;

    private LocalDate lastActiveOn;
}
