package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dao.ReviewDao;
import com.ecommerce.ecommerce_backend.dto.ReviewRequest;
import com.ecommerce.ecommerce_backend.dto.ReviewResponse;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDao reviewDao;
    private final ProductDao productDao;

    @Transactional
    public ReviewResponse createReview(LocalUser user, Long productId, ReviewRequest request) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException("Product was not found");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewDao.save(review);

        return new ReviewResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException("Product was not found");
        }

        return reviewDao.findAllByProduct(product, pageable).map(ReviewResponse::new);
    }

    @Transactional
    public ReviewResponse updateMyReview(LocalUser user, Long reviewId, ReviewRequest request) {
        Review review = reviewDao.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Review was not found"));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewDao.save(review);

        return new ReviewResponse(savedReview);
    }
}