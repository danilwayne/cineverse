package com.cineverse.auth.presentation;

import com.cineverse.auth.application.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    public record RegisterRequest(@NotBlank String name,
                                  @Email @NotBlank String email,
                                  @NotBlank @Size(min = 8) String password,
                                  String locale) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record ForgotPasswordRequest(@Email @NotBlank String email) {}

    public record ResetPasswordRequest(@NotBlank String token,
                                       @NotBlank @Size(min = 8) String password) {}

    @PostMapping("/register")
    public ResponseEntity<AuthService.Tokens> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req.name(), req.email(), req.password(), req.locale()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthService.Tokens> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.email(), req.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthService.Tokens> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.requestPasswordReset(req.email());
        // Resposta genérica de propósito: nunca revela se o e-mail existe.
        return ResponseEntity.ok(Map.of("message",
                "Se este e-mail estiver cadastrado, enviaremos um link para redefinir a senha."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.token(), req.password());
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso. Faça login com a nova senha."));
    }
}
