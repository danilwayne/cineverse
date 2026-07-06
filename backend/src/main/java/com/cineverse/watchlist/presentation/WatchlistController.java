package com.cineverse.watchlist.presentation;

import com.cineverse.auth.domain.User;
import com.cineverse.catalog.domain.Title;
import com.cineverse.watchlist.application.WatchlistService;
import com.cineverse.watchlist.domain.Watchlist;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService service;

    public record CreateRequest(@NotBlank String name) {}

    @GetMapping
    public List<Watchlist> list(@RequestParam Long profileId, @AuthenticationPrincipal User user) {
        return service.list(user, profileId);
    }

    @PostMapping
    public Watchlist create(@RequestParam Long profileId,
                            @Valid @RequestBody CreateRequest req,
                            @AuthenticationPrincipal User user) {
        return service.create(user, profileId, req.name());
    }

    @GetMapping("/{id}/items")
    public List<Title> items(@PathVariable Long id, @RequestParam Long profileId,
                             @AuthenticationPrincipal User user) {
        return service.items(user, profileId, id);
    }

    @PostMapping("/{id}/items/{titleId}")
    public ResponseEntity<Void> addItem(@PathVariable Long id, @PathVariable Long titleId,
                                        @RequestParam Long profileId,
                                        @AuthenticationPrincipal User user) {
        service.addItem(user, profileId, id, titleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/items/{titleId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long titleId,
                                           @RequestParam Long profileId,
                                           @AuthenticationPrincipal User user) {
        service.removeItem(user, profileId, id, titleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public Map<String, String> share(@PathVariable Long id, @RequestParam Long profileId,
                                     @AuthenticationPrincipal User user) {
        return Map.of("slug", service.share(user, profileId, id));
    }
}
