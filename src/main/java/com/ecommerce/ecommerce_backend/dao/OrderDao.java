package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface OrderDao extends JpaRepository<Order, Long> {

    Page<Order> findAllByUser(
            LocalUser user,
            Pageable pageable
    );

    Page<Order> findAllByUserAndStatus(
            LocalUser user,
            OrderStatus status,
            Pageable pageable
    );

    Page<Order> findAllByStatus(
            OrderStatus status,
            Pageable pageable
    );

    long countByStatus(OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(
            attributePaths = {
                    "quantities",
                    "quantities.product"
            }
    )
    Optional<Order> findLockedById(Long id);
}