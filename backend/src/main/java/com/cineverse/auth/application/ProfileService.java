package com.cineverse.auth.application;

import com.cineverse.auth.domain.Profile;
import com.cineverse.auth.domain.User;
import com.cineverse.auth.infrastructure.ProfileRepository;
import com.cineverse.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_PROFILES = 4;

    private final ProfileRepository profileRepository;
    private final GamificationService gamificationService;

    public List<Profile> list(User user) {
        return profileRepository.findByUserId(user.getId());
    }

    @Transactional
    public Profile create(User user, String name, boolean isKids) {
        if (profileRepository.findByUserId(user.getId()).size() >= MAX_PROFILES) {
            throw new IllegalArgumentException("Limite de " + MAX_PROFILES + " perfis atingido");
        }
        Profile profile = profileRepository.save(Profile.builder()
                .userId(user.getId())
                .name(name)
                .isKids(isKids)
                .build());
        gamificationService.initProfile(profile.getId());
        return profile;
    }

    /** Garante que o perfil pertence ao usuário logado (usado por todos os módulos) */
    public Profile requireOwnership(User user, Long profileId) {
        return profileRepository.findByIdAndUserId(profileId, user.getId())
                .orElseThrow(() -> new SecurityException("Perfil não pertence ao usuário"));
    }
}
