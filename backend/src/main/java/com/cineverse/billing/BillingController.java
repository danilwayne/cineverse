package com.cineverse.billing;

import com.cineverse.auth.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService service;

    public record DonationRequest(@NotNull @Min(100) Integer amountCents) {}
    public record WebhookRequest(@NotBlank String gatewaySubId, @NotBlank String event) {}

    @PostMapping("/donations/pix")
    public Payment donate(@Valid @RequestBody DonationRequest req,
                          @AuthenticationPrincipal User user) {
        return service.createPixDonation(user, req.amountCents());
    }

    @PostMapping("/subscriptions")
    public Subscription subscribe(@AuthenticationPrincipal User user) {
        return service.startSubscription(user);
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal User user) {
        service.cancelAtPeriodEnd(user);
        return ResponseEntity.noContent().build();
    }

    /** TODO produção: mover p/ endpoint público com validação HMAC do gateway */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@Valid @RequestBody WebhookRequest req) {
        service.handleWebhook(req.gatewaySubId(), req.event());
        return ResponseEntity.ok().build();
    }
}
