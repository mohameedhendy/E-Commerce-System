package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dao.ReviewDao;
import com.ecommerce.ecommerce_backend.dto.ReviewRequest;
import com.ecommerce.ecommerce_backend.dto.ReviewResponse;
import com.ecommerce.ecommerce_backend.exception.ForbiddenActionException;
import com.ecommerce.ecommerce_backend.exception.ResourceConflictException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.Review;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private ProductDao productDao;

    @Mock
    private OrderDao orderDao;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    public void confirmedPurchaseAllowsReviewCreation() {

        LocalUser user =
                createUser();

        Product product =
                createProduct();

        ReviewRequest request =
                createReviewRequest(
                        5,
                        "Excellent product"
                );

        when(productDao.findById(product.getId()))
                .thenReturn(
                        Optional.of(product)
                );

        when(
                orderDao
                        .existsByUserAndStatusAndQuantities_Product_Id(
                                user,
                                OrderStatus.CONFIRMED,
                                product.getId()
                        )
        ).thenReturn(true);

        when(
                reviewDao.existsByUserAndProduct(
                        user,
                        product
                )
        ).thenReturn(false);

        when(reviewDao.save(any(Review.class)))
                .thenAnswer(invocation -> {

                    Review review =
                            invocation.getArgument(0);

                    review.setId(10L);
                    review.setCreatedAt(
                            LocalDateTime.of(
                                    2026,
                                    7,
                                    16,
                                    12,
                                    0
                            )
                    );

                    return review;
                });

        ReviewResponse response =
                reviewService.createReview(
                        user,
                        product.getId(),
                        request
                );

        ArgumentCaptor<Review> reviewCaptor =
                ArgumentCaptor.forClass(
                        Review.class
                );

        verify(reviewDao).save(
                reviewCaptor.capture()
        );

        Review savedReview =
                reviewCaptor.getValue();

        Assertions.assertSame(
                user,
                savedReview.getUser()
        );

        Assertions.assertSame(
                product,
                savedReview.getProduct()
        );

        Assertions.assertEquals(
                5,
                savedReview.getRating()
        );

        Assertions.assertEquals(
                "Excellent product",
                savedReview.getComment()
        );

        Assertions.assertEquals(
                10L,
                response.getId()
        );

        Assertions.assertEquals(
                "Mohamed Hendy",
                response.getCustomerName()
        );
    }

    @Test
    public void userWithoutConfirmedPurchaseCannotReviewProduct() {

        LocalUser user =
                createUser();

        Product product =
                createProduct();

        ReviewRequest request =
                createReviewRequest(
                        4,
                        "Good product"
                );

        when(productDao.findById(product.getId()))
                .thenReturn(
                        Optional.of(product)
                );

        when(
                orderDao
                        .existsByUserAndStatusAndQuantities_Product_Id(
                                user,
                                OrderStatus.CONFIRMED,
                                product.getId()
                        )
        ).thenReturn(false);

        ForbiddenActionException exception =
                Assertions.assertThrows(
                        ForbiddenActionException.class,
                        () -> reviewService.createReview(
                                user,
                                product.getId(),
                                request
                        )
                );

        Assertions.assertEquals(
                "Only customers with a confirmed order can review this product",
                exception.getMessage()
        );

        verify(
                reviewDao,
                never()
        ).existsByUserAndProduct(
                any(LocalUser.class),
                any(Product.class)
        );

        verify(
                reviewDao,
                never()
        ).save(any(Review.class));
    }

    @Test
    public void duplicateReviewIsRejected() {

        LocalUser user =
                createUser();

        Product product =
                createProduct();

        ReviewRequest request =
                createReviewRequest(
                        5,
                        "Excellent product"
                );

        when(productDao.findById(product.getId()))
                .thenReturn(
                        Optional.of(product)
                );

        when(
                orderDao
                        .existsByUserAndStatusAndQuantities_Product_Id(
                                user,
                                OrderStatus.CONFIRMED,
                                product.getId()
                        )
        ).thenReturn(true);

        when(
                reviewDao.existsByUserAndProduct(
                        user,
                        product
                )
        ).thenReturn(true);

        ResourceConflictException exception =
                Assertions.assertThrows(
                        ResourceConflictException.class,
                        () -> reviewService.createReview(
                                user,
                                product.getId(),
                                request
                        )
                );

        Assertions.assertEquals(
                "You have already reviewed this product",
                exception.getMessage()
        );

        verify(
                reviewDao,
                never()
        ).save(any(Review.class));
    }

    @Test
    public void reviewOwnerCanUpdateReview() {

        LocalUser user =
                createUser();

        Product product =
                createProduct();

        Review review =
                new Review();

        review.setId(20L);
        review.setUser(user);
        review.setProduct(product);
        review.setRating(3);
        review.setComment("Original comment");
        review.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        7,
                        15,
                        12,
                        0
                )
        );

        ReviewRequest request =
                createReviewRequest(
                        5,
                        "Updated comment"
                );

        when(
                reviewDao.findByIdAndUser(
                        review.getId(),
                        user
                )
        ).thenReturn(
                Optional.of(review)
        );

        when(reviewDao.save(review))
                .thenReturn(review);

        ReviewResponse response =
                reviewService.updateMyReview(
                        user,
                        review.getId(),
                        request
                );

        Assertions.assertEquals(
                5,
                review.getRating()
        );

        Assertions.assertEquals(
                "Updated comment",
                review.getComment()
        );

        Assertions.assertEquals(
                5,
                response.getRating()
        );

        Assertions.assertEquals(
                "Updated comment",
                response.getComment()
        );

        verify(reviewDao).save(review);

        verify(
                orderDao,
                never()
        ).existsByUserAndStatusAndQuantities_Product_Id(
                any(LocalUser.class),
                any(OrderStatus.class),
                any(Long.class)
        );
    }

    private LocalUser createUser() {

        LocalUser user =
                new LocalUser();

        user.setId(1L);
        user.setUsername("mohamed");
        user.setFirstName("Mohamed");
        user.setLastName("Hendy");

        return user;
    }

    private Product createProduct() {

        Product product =
                new Product();

        product.setId(1L);
        product.setName("Test Product");
        product.setActive(true);

        return product;
    }

    private ReviewRequest createReviewRequest(
            Integer rating,
            String comment
    ) {

        ReviewRequest request =
                new ReviewRequest();

        request.setRating(rating);
        request.setComment(comment);

        return request;
    }
}