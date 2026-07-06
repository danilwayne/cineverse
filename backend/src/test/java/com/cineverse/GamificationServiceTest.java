package com.cineverse;

import com.cineverse.gamification.GamificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração do motor de gamificação com Postgres real
 * (imagem pgvector, pois o Flyway cria a extensão vector). Requer Docker.
 */
@SpringBootTest
@Testcontainers
class GamificationServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private GamificationService service;

    @Test
    void deveAcumularXpEConcederConquistaDePrimeiraReview() {
        Long profileId = 999L;
        service.initProfile(profileId);
        service.onReview(profileId, 1);

        var status = service.status(profileId);
        assertThat(status.xp()).isGreaterThanOrEqualTo(70);   // 20 da review + 50 da conquista
        assertThat(status.streakDays()).isEqualTo(1);
        assertThat(status.achievements())
                .anyMatch(a -> a.getCode().equals("FIRST_REVIEW"));
    }

    @Test
    void deveSubirDeNivelACada100Xp() {
        Long profileId = 998L;
        service.initProfile(profileId);
        for (int i = 0; i < 6; i++) {
            service.onEvent(profileId, GamificationService.GameEvent.REVIEW);
        }
        assertThat(service.status(profileId).level()).isGreaterThanOrEqualTo(2);
    }
}
