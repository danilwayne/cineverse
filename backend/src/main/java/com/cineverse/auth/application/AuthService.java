package com.cineverse.auth.application;

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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GamificationService gamificationService;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshDays;

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
