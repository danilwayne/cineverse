package com.cineverse.catalog.application;

import com.cineverse.catalog.domain.Title;
import com.cineverse.catalog.infrastructure.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Recomendações v1: sobreposição de gêneros com o histórico do perfil,
 * ordenada por nota. A coluna embedding (pgvector) já existe no schema
 * p/ evoluir para similaridade semântica (v2) sem migração.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TitleRepository titleRepository;

    public List<Title> forProfile(Long profileId, int limit) {
        List<Title> recs = titleRepository.recommendByGenres(profileId, limit);
        return recs.isEmpty() ? titleRepository.findTop20ByOrderByVoteAverageDesc() : recs;
    }
}
