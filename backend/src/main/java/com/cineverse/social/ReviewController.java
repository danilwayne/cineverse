package com.cineverse.social;

import com.cineverse.auth.application.ProfileService;
import com.cineverse.auth.domain.User;
import com.cineverse.gamification.GamificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/titles/{titleId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository repository;
    private final ProfileService profileService;
    private final GamificationService gamificationService;

    public record CreateRequest(@NotNull @Min(1) @Max(10) Short rating,
                                @Size(max = 5000) String body,
                                boolean spoiler) {}

    @GetMapping
    public List<Review> list(@PathVariable Long titleId) {
        return repository.findByTitleIdAndStatusOrderByCreatedAtDesc(titleId, "PUBLISHED");
    }

    @PostMapping
    @Transactional
    public Review create(@PathVariable Long titleId,
                         @RequestParam Long profileId,
                         @Valid @RequestBody CreateRequest req,
                         @AuthenticationPrincipal User user) {
        profileService.requireOwnership(user, profileId);
        Review review = repository.findByProfileIdAndTitleId(profileId, titleId)
                .orElseGet(() -> Review.builder().profileId(profileId).titleId(titleId)
                        .rating(req.rating()).build());
        review.setRating(req.rating());
        review.setBody(req.body());
        review.setSpoiler(req.spoiler());
        Review saved = repository.save(review);

        gamificationService.onReview(profileId, repository.countByProfileId(profileId));
        return saved;
    }
}
