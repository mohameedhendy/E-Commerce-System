package com.ecommerce.ecommerce_backend.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecommerce.ecommerce_backend.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ecommerce.ecommerce_backend.dto.ReviewRequest;
import com.ecommerce.ecommerce_backend.dto.ReviewResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reviews", description = "Authenticated customer review management")
@RestController
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Update an authenticated customer's review")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateMyReview(@AuthenticationPrincipal LocalUser user,
                                                         @PathVariable Long reviewId,
                                                         @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateMyReview(user, reviewId, request));
    }
}