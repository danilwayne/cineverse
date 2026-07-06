package com.cineverse.admin;

import com.cineverse.auth.infrastructure.UserRepository;
import com.cineverse.shared.audit.ProductEventRepository;
import com.cineverse.social.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Painel administrativo — protegido por ROLE_ADMIN no SecurityConfig */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ProductEventRepository eventRepository;

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "totalReviews", reviewRepository.count(),
                "dailyActiveProfiles", eventRepository.dailyActiveProfiles());
    }
}
