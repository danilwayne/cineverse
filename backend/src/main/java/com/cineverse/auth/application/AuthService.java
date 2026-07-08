package com.cineverse.auth.application;

import com.cineverse.auth.domain.PasswordResetToken;
import com.cineverse.auth.domain.Profile;
import com.cineverse.auth.domain.RefreshToken;
import com.cineverse.auth.domain.User;
import com.cineverse.auth.infrastructure.*;
import com.cineverse.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GamificationService gamificationService;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshDays;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.password-reset.expiration-minutes}")
    private long resetExpirationMinutes;

    public record Tokens(String accessToken, String refreshToken, String name, String plan) {}

    @Transactional
    public Tokens register(String name, String email, String password, String locale) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        User user = userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .locale(locale != null ? locale : "pt-BR")
                .build());

        Profile profile = profileRepository.save(Profile.builder()
                .userId(user.getId())
                .name(name)
                .build());

        gamificationService.initProfile(profile.getId());
        return issueTokens(user);
    }

    @Transactional
    public Tokens login(String email, String password) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new SecurityException("E-mail ou senha inválidos"));
        return issueTokens(user);
    }

    @Transactional
    public Tokens refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(jwtService.sha256(refreshToken))
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new SecurityException("Refresh token inválido ou expirado"));

        stored.setRevoked(true); // rotação: cada refresh só vale uma vez
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedFalse(jwtService.sha256(refreshToken))
                .ifPresent(t -> t.setRevoked(true));
    }

    /** Passo 1 do "esqueci minha senha": gera o token e envia o link por e-mail.
     *  Nunca revela se o e-mail existe (proteção contra descoberta de contas). */
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            String rawToken = jwtService.generateOpaqueToken();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(jwtService.sha256(rawToken))
                    .expiresAt(OffsetDateTime.now().plusMinutes(resetExpirationMinutes))
                    .build());

            // frontendUrl pode ser lista separada por vírgula (CORS); o link usa a 1ª origem.
            String baseUrl = frontendUrl.split(",")[0].trim();
            String link = baseUrl + "/redefinir-senha?token=" + rawToken;
            emailService.sendPasswordReset(user.getEmail(), link);
        });
    }

    /** Troca de senha do usuário LOGADO: exige a senha atual (validada com BCrypt). */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        User managed = userRepository.findById(user.getId())
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        managed.setPasswordHash(passwordEncoder.encode(newPassword));

        // Desloga as outras sessões (o próprio front vai pegar novo token no próximo login se precisar).
        refreshTokenRepository.findByUserIdAndRevokedFalse(managed.getId())
                .forEach(rt -> rt.setRevoked(true));
    }

    /** Passo 2: valida o token e grava a nova senha. */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHashAndUsedFalse(jwtService.sha256(rawToken))
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Link inválido ou expirado. Peça um novo."));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsed(true); // uso único

        // Segurança: desloga todas as sessões antigas (força novo login com a senha nova)
        refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId())
                .forEach(rt -> rt.setRevoked(true));
    }

    private Tokens issueTokens(User user) {
        String refresh = jwtService.generateOpaqueToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(jwtService.sha256(refresh))
                .expiresAt(OffsetDateTime.now().plusDays(refreshDays))
                .build());
        return new Tokens(jwtService.generateAccessToken(user), refresh,
                user.getName(), user.getPlan().name());
    }
}
