package com.cineverse.auth.infrastructure;

import com.cineverse.auth.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByUserId(Long userId);
    Optional<Profile> findByIdAndUserId(Long id, Long userId);
}
