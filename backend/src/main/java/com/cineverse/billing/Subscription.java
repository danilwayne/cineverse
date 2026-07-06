package com.cineverse.billing;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String gateway;               // MERCADO_PAGO | STRIPE

    @Column(name = "gateway_sub_id", unique = true)
    private String gatewaySubId;

    @Column(nullable = false)
    private String status;                // ACTIVE | PAST_DUE | CANCELLED

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "current_period_end")
    private OffsetDateTime currentPeriodEnd;

    @Builder.Default
    @Column(name = "cancel_at_period_end")
    private boolean cancelAtPeriodEnd = false;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
