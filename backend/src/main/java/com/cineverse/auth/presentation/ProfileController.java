package com.cineverse.auth.presentation;

import com.cineverse.auth.application.ProfileService;
import com.cineverse.auth.domain.Profile;
import com.cineverse.auth.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    public record CreateProfileRequest(@NotBlank String name, boolean isKids) {}

    @GetMapping
    public List<Profile> list(@AuthenticationPrincipal User user) {
        return profileService.list(user);
    }

    @PostMapping
    public Profile create(@Valid @RequestBody CreateProfileRequest req,
                          @AuthenticationPrincipal User user) {
        return profileService.create(user, req.name(), req.isKids());
    }
}
