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

    private static final String PRODUCT_NOT_FOUND =
            "Product was not found";

    private static final String REVIEW_NOT_FOUND =
            "Review was not found";

    private final ReviewDao reviewDao;
    private final ProductDao productDao;

    @Transactional
    public ReviewResponse createReview(
            LocalUser user,
            Long productId,
            ReviewRequest request
    ) {

        Product product =
                getActiveProductOrThrow(productId);

        Review review = new Review();

        review.setProduct(product);
        review.setUser(user);

        applyReviewDetails(
                review,
                request
        );

        return saveAndConvert(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(
            Long productId,
            Pageable pageable
    ) {

        Product product =
                getActiveProductOrThrow(productId);

        Page<Review> reviews =
                reviewDao.findAllByProduct(
                        product,
                        pageable
                );

        return toReviewResponses(reviews);
    }

    @Transactional
    public ReviewResponse updateMyReview(
            LocalUser user,
            Long reviewId,
            ReviewRequest request
    ) {

        Review review =
                getUserReviewOrThrow(
                        reviewId,
                        user
                );

        applyReviewDetails(
                review,
                request
        );

        return saveAndConvert(review);
    }

    private Product getActiveProductOrThrow(
            Long productId
    ) {

        Product product = productDao
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PRODUCT_NOT_FOUND
                        )
                );

        if (!Boolean.TRUE.equals(
                product.getActive()
        )) {
            throw new ResourceNotFoundException(
                    PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private Review getUserReviewOrThrow(
            Long reviewId,
            LocalUser user
    ) {

        return reviewDao
                .findByIdAndUser(
                        reviewId,
                        user
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                REVIEW_NOT_FOUND
                        )
                );
    }

    private void applyReviewDetails(
            Review review,
            ReviewRequest request
    ) {

        review.setRating(
                request.getRating()
        );

        review.setComment(
                request.getComment()
        );
    }

    private ReviewResponse saveAndConvert(
            Review review
    ) {

        Review savedReview =
                reviewDao.save(review);

        return new ReviewResponse(
                savedReview
        );
    }

    private Page<ReviewResponse> toReviewResponses(
            Page<Review> reviews
    ) {

        return reviews.map(
                ReviewResponse::new
        );
    }
}