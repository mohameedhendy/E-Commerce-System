package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartDao extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    @EntityGraph(
            attributePaths = {
                    "items",
                    "items.product",
                    "items.product.stock"
            }
    )
    @Query("""
            SELECT DISTINCT cart
            FROM Cart cart
            WHERE cart.user.id = :userId
            """)
    Optional<Cart> findDetailedByUserId(
            @Param("userId") Long userId
    );
}