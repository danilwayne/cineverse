package com.cineverse.billing;

import com.cineverse.auth.domain.User;
import com.cineverse.auth.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ⚠️ INTEGRAÇÃO DE PAGAMENTO = STUB.
 * A estrutura (tabelas, fluxo, webhook) está pronta, mas a chamada real ao
 * gateway precisa ser implementada com o SDK do Mercado Pago:
 *   https://github.com/mercadopago/sdk-java
 * Passos: criar preferência/assinatura -> receber webhook -> confirmar pagamento.
 * NUNCA ative o plano Premium sem confirmação real do webhook em produção.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    public static final int PREMIUM_PRICE_CENTS = 990; // R$ 9,90/mês

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /** Cria doação Pix PENDENTE. TODO: gerar cobrança real no Mercado Pago e retornar o QR Code. */
    @Transactional
    public Payment createPixDonation(User user, int amountCents) {
        if (amountCents < 100) throw new IllegalArgumentException("Doação mínima: R$ 1,00");
        return paymentRepository.save(Payment.builder()
                .userId(user.getId())
                .kind("DONATION")
                .method("PIX")
                .amountCents(amountCents)
                .status("PENDING")
                .gatewayRef("STUB-" + UUID.randomUUID())
                .build());
    }

    /** Inicia assinatura PENDENTE. TODO: criar assinatura real no gateway e retornar checkout URL. */
    @Transactional
    public Subscription startSubscription(User user) {
        subscriptionRepository.findByUserIdAndStatus(user.getId(), "ACTIVE")
                .ifPresent(s -> { throw new IllegalArgumentException("Assinatura já ativa"); });
        return subscriptionRepository.save(Subscription.builder()
                .userId(user.getId())
                .gateway("MERCADO_PAGO")
                .gatewaySubId("STUB-" + UUID.randomUUID())
                .status("PAST_DUE") // vira ACTIVE somente via webhook confirmado
                .priceCents(PREMIUM_PRICE_CENTS)
                .build());
    }

    /**
     * Webhook do gateway. TODO: validar assinatura HMAC do Mercado Pago antes de processar!
     * Sem validação, qualquer pessoa poderia se dar Premium grátis.
     */
    @Transactional
    public void handleWebhook(String gatewaySubId, String event) {
        subscriptionRepository.findAll().stream()
                .filter(s -> s.getGatewaySubId().equals(gatewaySubId))
                .findFirst()
                .ifPresent(sub -> {
                    if ("payment.approved".equals(event)) {
                        sub.setStatus("ACTIVE");
                        sub.setCurrentPeriodEnd(OffsetDateTime.now().plusMonths(1));
                        userRepository.findById(sub.getUserId()).ifPresent(u -> {
                            u.setPlan(User.Plan.PREMIUM);
                            u.setPremiumUntil(sub.getCurrentPeriodEnd());
                        });
                        log.info("Assinatura {} ativada", sub.getId());
                    }
                });
    }

    /** Cancelamento honesto: mantém acesso até o fim do período já pago. */
    @Transactional
    public void cancelAtPeriodEnd(User user) {
        subscriptionRepository.findByUserIdAndStatus(user.getId(), "ACTIVE")
                .ifPresentOrElse(s -> s.setCancelAtPeriodEnd(true),
                        () -> { throw new IllegalArgumentException("Nenhuma assinatura ativa"); });
    }
}
