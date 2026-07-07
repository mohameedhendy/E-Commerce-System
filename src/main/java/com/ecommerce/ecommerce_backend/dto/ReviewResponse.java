package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String customerName;

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
        this.customerName = review.getUser().getFirstName() + " " + review.getUser().getLastName();
    }
}