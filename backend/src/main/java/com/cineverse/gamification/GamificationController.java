package com.cineverse.gamification;

import com.cineverse.auth.application.ProfileService;
import com.cineverse.auth.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService service;
    private final ProfileService profileService;

    /** XP, nível, streak e conquistas do perfil */
    @GetMapping("/me")
    public GamificationService.Status me(@RequestParam Long profileId,
                                         @AuthenticationPrincipal User user) {
        profileService.requireOwnership(user, profileId);
        return service.status(profileId);
    }
}
