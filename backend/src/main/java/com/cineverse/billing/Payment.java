package com.cineverse.billing;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(nullable = false)
    private String kind;          // SUBSCRIPTION | DONATION

    @Column(nullable = false)
    private String method;        // PIX | CARD

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Column(nullable = false)
    private String status;        // PENDING | PAID | FAILED | REFUNDED

    @Column(name = "gateway_ref")
    private String gatewayRef;

    private OffsetDateTime paidAt;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
