package com.cineverse.auth.application;

import com.cineverse.auth.domain.Profile;
import com.cineverse.auth.domain.User;
import com.cineverse.auth.infrastructure.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolve o perfil ativo enviado no header X-Profile-Id,
 * garantindo que pertence ao usuário autenticado.
 */
@Service
@RequiredArgsConstructor
public class ProfileResolver {

    private final ProfileRepository profileRepository;

    public Profile resolve(User user, Long profileId) {
        if (profileId == null) {
            return profileRepository.findByUserId(user.getId()).stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Usuário sem perfil"));
        }
        return profileRepository.findByIdAndUserId(profileId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Perfil não pertence ao usuário"));
    }
}
