package com.cineverse.catalog.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente da API do TMDB (https://developer.themoviedb.org).
 * Fonte 100% legal de metadados, pôsteres e trailers.
 * Crie sua chave gratuita e defina TMDB_API_KEY.
 */
@Component
public class TmdbClient {

    private final RestClient client;
    private final String apiKey;

    public TmdbClient(@Value("${app.tmdb.base-url}") String baseUrl,
                      @Value("${app.tmdb.api-key}") String apiKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public JsonNode trending(String lang) {
        return get("/trending/all/week?language={lang}&api_key={key}", lang);
    }

    public JsonNode searchMulti(String query, String lang) {
        return client.get()
                .uri("/search/multi?query={q}&language={lang}&include_adult=false&api_key={key}",
                        query, lang, apiKey)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode details(String mediaType, Long tmdbId, String lang) {
        return client.get()
                .uri("/{type}/{id}?language={lang}&append_to_response=videos&api_key={key}",
                        mediaType, tmdbId, lang, apiKey)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode watchProviders(String mediaType, Long tmdbId) {
        return client.get()
                .uri("/{type}/{id}/watch/providers?api_key={key}", mediaType, tmdbId, apiKey)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode get(String uri, String lang) {
        return client.get().uri(uri, lang, apiKey).retrieve().body(JsonNode.class);
    }
}
