package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewDao
        extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = "user")
    Page<Review> findAllByProduct(
            Product product,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "user")
    Optional<Review> findByIdAndUser(
            Long id,
            LocalUser user
    );
}