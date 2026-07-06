package com.cineverse.watchlist.application;

import com.cineverse.auth.application.ProfileService;
import com.cineverse.auth.domain.User;
import com.cineverse.catalog.domain.Title;
import com.cineverse.catalog.infrastructure.TitleRepository;
import com.cineverse.gamification.GamificationService;
import com.cineverse.watchlist.domain.Watchlist;
import com.cineverse.watchlist.domain.WatchlistItem;
import com.cineverse.watchlist.infrastructure.WatchlistItemRepository;
import com.cineverse.watchlist.infrastructure.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository itemRepository;
    private final TitleRepository titleRepository;
    private final ProfileService profileService;
    private final GamificationService gamificationService;

    private static final String SLUG_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private final SecureRandom random = new SecureRandom();

    public List<Watchlist> list(User user, Long profileId) {
        profileService.requireOwnership(user, profileId);
        return watchlistRepository.findByProfileId(profileId);
    }

    @Transactional
    public Watchlist create(User user, Long profileId, String name) {
        profileService.requireOwnership(user, profileId);
        Watchlist list = watchlistRepository.save(Watchlist.builder()
                .profileId(profileId)
                .name(name)
                .build());
        gamificationService.onEvent(profileId, GamificationService.GameEvent.LIST_CREATED);
        return list;
    }

    @Transactional
    public void addItem(User user, Long profileId, Long watchlistId, Long titleId) {
        Watchlist list = requireOwnedList(user, profileId, watchlistId);
        titleRepository.findById(titleId)
                .orElseThrow(() -> new NoSuchElementException("Título não sincronizado; abra os detalhes dele primeiro"));
        itemRepository.save(WatchlistItem.builder()
                .watchlistId(list.getId())
                .titleId(titleId)
                .addedBy(profileId)
                .build());
        gamificationService.onEvent(profileId, GamificationService.GameEvent.WATCHLIST_ADD);
    }

    @Transactional
    public void removeItem(User user, Long profileId, Long watchlistId, Long titleId) {
        requireOwnedList(user, profileId, watchlistId);
        itemRepository.deleteByWatchlistIdAndTitleId(watchlistId, titleId);
    }

    public List<Title> items(User user, Long profileId, Long watchlistId) {
        requireOwnedList(user, profileId, watchlistId);
        return loadTitles(watchlistId);
    }

    /** Gera (ou retorna) o link público da lista */
    @Transactional
    public String share(User user, Long profileId, Long watchlistId) {
        Watchlist list = requireOwnedList(user, profileId, watchlistId);
        if (list.getShareSlug() == null) {
            StringBuilder slug = new StringBuilder(12);
            for (int i = 0; i < 12; i++) {
                slug.append(SLUG_CHARS.charAt(random.nextInt(SLUG_CHARS.length())));
            }
            list.setShareSlug(slug.toString());
        }
        return list.getShareSlug();
    }

    /** Acesso público (sem login) via slug */
    public PublicList publicView(String slug) {
        Watchlist list = watchlistRepository.findByShareSlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Lista não encontrada"));
        return new PublicList(list.getName(), loadTitles(list.getId()));
    }

    private Watchlist requireOwnedList(User user, Long profileId, Long watchlistId) {
        profileService.requireOwnership(user, profileId);
        Watchlist list = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new NoSuchElementException("Lista não encontrada"));
        if (!list.getProfileId().equals(profileId)) {
            throw new SecurityException("Lista não pertence ao perfil");
        }
        return list;
    }

    private List<Title> loadTitles(Long watchlistId) {
        List<Long> ids = itemRepository.findByWatchlistId(watchlistId).stream()
                .map(WatchlistItem::getTitleId).toList();
        return titleRepository.findAllById(ids);
    }

    public record PublicList(String name, List<Title> titles) {}
}
