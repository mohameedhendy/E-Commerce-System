package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemDao
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(
            Long cartId,
            Long productId
    );

    List<CartItem> findAllByCart_IdOrderByIdAsc(
            Long cartId
    );

    long countByCart_Id(Long cartId);
}