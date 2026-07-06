package com.cineverse.catalog.application;

import com.cineverse.catalog.domain.Title;
import com.cineverse.catalog.domain.WatchProvider;
import com.cineverse.catalog.infrastructure.TitleRepository;
import com.cineverse.catalog.infrastructure.TmdbClient;
import com.cineverse.catalog.infrastructure.WatchProviderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final TmdbClient tmdb;
    private final TitleRepository titleRepository;
    private final WatchProviderRepository providerRepository;

    /** Destaques da semana (cache Redis 30 min) */
    @Cacheable(value = "trending", key = "#lang")
    public String trending(String lang) {
        return tmdb.trending(lang).toString();
    }

    @Cacheable(value = "search", key = "#lang + ':' + #query.toLowerCase()")
    public String search(String query, String lang) {
        return tmdb.searchMulti(query, lang).toString();
    }

    /**
     * Detalhes de um título: busca no TMDB, sincroniza no banco local
     * (necessário p/ watchlists, reviews e recomendações) e retorna com providers.
     */
    @Transactional
    public TitleDetails details(String mediaType, Long tmdbId, String lang) {
        JsonNode data = tmdb.details(mediaType, tmdbId, lang);
        Title title = upsert(mediaType, tmdbId, data);
        syncProviders(title, mediaType, tmdbId);
        return new TitleDetails(title, data,
                providerRepository.findByTitleIdAndCountry(title.getId(), "BR"));
    }

    private Title upsert(String mediaType, Long tmdbId, JsonNode data) {
        Title title = titleRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .orElseGet(() -> Title.builder().tmdbId(tmdbId).mediaType(mediaType).build());

        boolean isMovie = "movie".equals(mediaType);
        title.setName(data.path(isMovie ? "title" : "name").asText());
        title.setOriginalName(data.path(isMovie ? "original_title" : "original_name").asText(null));
        title.setOverview(data.path("overview").asText(null));
        title.setPosterPath(data.path("poster_path").asText(null));
        title.setBackdropPath(data.path("backdrop_path").asText(null));
        String date = data.path(isMovie ? "release_date" : "first_air_date").asText("");
        title.setReleaseDate(date.isBlank() ? null : LocalDate.parse(date));
        if (data.hasNonNull("runtime")) title.setRuntimeMin(data.get("runtime").asInt());
        if (data.hasNonNull("vote_average"))
            title.setVoteAverage(BigDecimal.valueOf(data.get("vote_average").asDouble()));

        List<String> genres = new ArrayList<>();
        data.path("genres").forEach(g -> genres.add(g.path("name").asText()));
        title.setGenres(genres.toArray(String[]::new));
        title.setSyncedAt(OffsetDateTime.now());
        return titleRepository.save(title);
    }

    private void syncProviders(Title title, String mediaType, Long tmdbId) {
        try {
            JsonNode br = tmdb.watchProviders(mediaType, tmdbId).path("results").path("BR");
            providerRepository.deleteByTitleId(title.getId());
            providerRepository.flush();   // executa o DELETE antes dos INSERTs (senão colide com a UNIQUE)
            if (br.isMissingNode()) return;
            String link = br.path("link").asText(null);
            Set<String> seen = new HashSet<>();          // evita provider+kind duplicado no mesmo título
            for (String category : List.of("flatrate", "rent", "buy")) {
                String kind = "flatrate".equals(category) ? "stream" : category;
                for (JsonNode p : br.path(category)) {
                    String provider = p.path("provider_name").asText();
                    if (provider.isBlank() || !seen.add(provider + '|' + kind)) continue;
                    providerRepository.save(WatchProvider.builder()
                            .titleId(title.getId())
                            .provider(provider)
                            .kind(kind)
                            .url(link)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao sincronizar providers do título {}", tmdbId, e);
        }
    }

    public record TitleDetails(Title title, JsonNode tmdbData, List<WatchProvider> providers) {}
}
