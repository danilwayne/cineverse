package com.cineverse.gamification;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "profile_achievements")
@IdClass(ProfileAchievement.Pk.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileAchievement {

    @Id
    @Column(name = "profile_id")
    private Long profileId;

    @Id
    @Column(name = "achievement_id")
    private Long achievementId;

    @Builder.Default
    private OffsetDateTime earnedAt = OffsetDateTime.now();

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long profileId;
        private Long achievementId;
    }
}
