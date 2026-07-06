package com.cineverse.catalog.infrastructure;

import com.cineverse.catalog.domain.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TitleRepository extends JpaRepository<Title, Long> {

    Optional<Title> findByTmdbIdAndMediaType(Long tmdbId, String mediaType);

    /** Busca tolerante a erros de digitação (índice trigram) */
    @Query(value = """
            SELECT * FROM titles
            WHERE name % :q OR name ILIKE '%' || :q || '%'
            ORDER BY similarity(name, :q) DESC
            LIMIT 20
            """, nativeQuery = true)
    List<Title> searchByName(@Param("q") String query);

    /** Recomendações: títulos que compartilham gêneros com os favoritos do perfil */
    @Query(value = """
            SELECT t.* FROM titles t
            WHERE t.id NOT IN (
                SELECT a.title_id FROM activity a WHERE a.profile_id = :profileId)
              AND t.genres && (
                SELECT COALESCE(array_agg(DISTINCT g), '{}')
                FROM activity a
                JOIN titles w ON w.id = a.title_id, unnest(w.genres) g
                WHERE a.profile_id = :profileId AND a.status IN ('WATCHED','WATCHING'))
            ORDER BY t.vote_average DESC NULLS LAST
            LIMIT :limit
            """, nativeQuery = true)
    List<Title> recommendByGenres(@Param("profileId") Long profileId, @Param("limit") int limit);

    List<Title> findTop20ByOrderByVoteAverageDesc();
}
