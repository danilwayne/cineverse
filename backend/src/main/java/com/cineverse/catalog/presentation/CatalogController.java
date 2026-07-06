package com.cineverse.catalog.presentation;

import com.cineverse.auth.application.ProfileService;
import com.cineverse.auth.domain.User;
import com.cineverse.catalog.application.CatalogService;
import com.cineverse.catalog.application.RecommendationService;
import com.cineverse.catalog.domain.Title;
import com.cineverse.shared.audit.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final RecommendationService recommendationService;
    private final ProfileService profileService;
    private final EventService eventService;

    @GetMapping(value = "/trending", produces = MediaType.APPLICATION_JSON_VALUE)
    public String trending(@AuthenticationPrincipal User user) {
        return catalogService.trending(user.getLocale());
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public String search(@RequestParam String q,
                           @RequestParam(required = false) Long profileId,
                           @AuthenticationPrincipal User user) {
        eventService.track(profileId, "SEARCH", Map.of("q", q));
        return catalogService.search(q, user.getLocale());
    }

    @GetMapping("/{mediaType}/{tmdbId}")
    public CatalogService.TitleDetails details(@PathVariable String mediaType,
                                               @PathVariable Long tmdbId,
                                               @RequestParam(required = false) Long profileId,
                                               @AuthenticationPrincipal User user) {
        eventService.track(profileId, "TITLE_VIEW", Map.of("tmdbId", tmdbId, "type", mediaType));
        return catalogService.details(mediaType, tmdbId, user.getLocale());
    }

    @GetMapping("/recommendations")
    public List<Title> recommendations(@RequestParam Long profileId,
                                       @AuthenticationPrincipal User user) {
        profileService.requireOwnership(user, profileId);
        return recommendationService.forProfile(profileId, 20);
    }
}
