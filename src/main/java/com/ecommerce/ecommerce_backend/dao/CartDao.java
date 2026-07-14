package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartDao extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}