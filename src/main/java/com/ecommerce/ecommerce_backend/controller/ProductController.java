package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.PagedResponse;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.dto.ReviewRequest;
import com.ecommerce.ecommerce_backend.dto.ReviewResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.ProductService;
import com.ecommerce.ecommerce_backend.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Validated
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    public PagedResponse<ProductResponse> getAllProducts(
            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size must not exceed 50")
            int size,

            @RequestParam(defaultValue = "id")
            @Pattern(regexp = "id|name|price", message = "Sort field must be one of: id, name, price")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            @Pattern(regexp = "asc|desc|ASC|DESC", message = "Sort direction must be asc or desc")
            String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductResponse> products = productService.getAllProducts(keyword, pageable);

        return new PagedResponse<>(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PostMapping("/{productId}/review")
    public ResponseEntity<ReviewResponse> createReview(@AuthenticationPrincipal LocalUser user,
                                                       @PathVariable Long productId,
                                                       @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(user, productId, request));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(new PagedResponse<>(reviewService.getProductReviews(productId, pageable)));
    }
}