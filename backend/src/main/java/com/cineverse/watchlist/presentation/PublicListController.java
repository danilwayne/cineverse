package com.cineverse.watchlist.presentation;

import com.cineverse.watchlist.application.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/lists")
@RequiredArgsConstructor
public class PublicListController {

    private final WatchlistService service;

    /** Lista compartilhada — acessível sem login */
    @GetMapping("/{slug}")
    public WatchlistService.PublicList view(@PathVariable String slug) {
        return service.publicView(slug);
    }
}
