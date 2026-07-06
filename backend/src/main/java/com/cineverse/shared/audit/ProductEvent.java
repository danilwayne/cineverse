package com.cineverse.shared.audit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "product_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;

    @Column(nullable = false)
    private String event;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
