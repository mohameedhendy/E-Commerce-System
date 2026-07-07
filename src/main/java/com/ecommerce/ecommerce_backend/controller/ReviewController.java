package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ReviewRequest;
import com.ecommerce.ecommerce_backend.dto.ReviewResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateMyReview(@AuthenticationPrincipal LocalUser user,
                                                         @PathVariable Long reviewId,
                                                         @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateMyReview(user, reviewId, request));
    }
}