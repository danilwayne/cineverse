package com.cineverse.billing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PaymentRepository extends JpaRepository<Payment, Long> {}

interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, String status);
}
