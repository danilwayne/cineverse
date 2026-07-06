package com.cineverse.activity;

import com.cineverse.auth.application.ProfileService;
import com.cineverse.auth.domain.User;
import com.cineverse.gamification.GamificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityRepository repository;
    private final ProfileService profileService;
    private final GamificationService gamificationService;

    public record UpsertRequest(@NotNull Long titleId, @NotNull Activity.Status status,
                                Integer season, Integer episode) {}

    /** Histórico + continuar assistindo */
    @GetMapping
    public List<Activity> list(@RequestParam Long profileId, @AuthenticationPrincipal User user) {
        profileService.requireOwnership(user, profileId);
        return repository.findByProfileIdOrderByUpdatedAtDesc(profileId);
    }

    @PutMapping
    @Transactional
    public Activity upsert(@RequestParam Long profileId,
                           @Valid @RequestBody UpsertRequest req,
                           @AuthenticationPrincipal User user) {
        profileService.requireOwnership(user, profileId);
        Activity activity = repository.findByProfileIdAndTitleId(profileId, req.titleId())
                .orElseGet(() -> Activity.builder()
                        .profileId(profileId)
                        .titleId(req.titleId())
                        .status(req.status())
                        .build());
        activity.setStatus(req.status());
        activity.setSeason(req.season());
        activity.setEpisode(req.episode());
        activity.setUpdatedAt(OffsetDateTime.now());
        Activity saved = repository.save(activity);

        if (req.status() == Activity.Status.WATCHED) {
            long watched = repository.countByProfileIdAndStatus(profileId, Activity.Status.WATCHED);
            gamificationService.onWatched(profileId, watched);
        } else {
            gamificationService.onEvent(profileId, GamificationService.GameEvent.ACTIVITY_UPDATE);
        }
        return saved;
    }
}
