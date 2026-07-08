package com.cineverse.auth.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.password-reset.from}")
    private String from;

    /**
     * Envia o e-mail com o link de redefinição de senha.
     * Se o Gmail ainda não estiver configurado (MAIL_USERNAME vazio) ou o envio falhar,
     * o link é impresso no console para que o fluxo possa ser testado mesmo assim.
     */
    public void sendPasswordReset(String toEmail, String resetLink) {
        if (mailUsername == null || mailUsername.isBlank()) {
            logLink(toEmail, resetLink, "MAIL_USERNAME não configurado");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(toEmail);
            msg.setSubject("CineVerse — redefinição de senha");
            msg.setText("""
                    Olá!

                    Recebemos um pedido para redefinir a senha da sua conta CineVerse.
                    Clique no link abaixo para criar uma nova senha (válido por 30 minutos):

                    %s

                    Se você não pediu isso, pode ignorar este e-mail — sua senha continua a mesma.

                    Equipe CineVerse""".formatted(resetLink));
            mailSender.send(msg);
            log.info("E-mail de redefinição enviado para {}", toEmail);
        } catch (Exception e) {
            logLink(toEmail, resetLink, "falha ao enviar e-mail: " + e.getMessage());
        }
    }

    private void logLink(String toEmail, String resetLink, String motivo) {
        log.warn("""

                >>> RESET DE SENHA ({}) para {}
                >>> Link: {}
                >>> (copie e cole no navegador para testar)
                """, motivo, toEmail, resetLink);
    }
}
