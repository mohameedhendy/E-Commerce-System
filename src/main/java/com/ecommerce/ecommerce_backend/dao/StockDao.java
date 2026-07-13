package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockDao extends JpaRepository<Stock, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE Stock s
            SET s.quantity = s.quantity - :quantity,
                s.version = s.version + 1
            WHERE s.product.id = :productId
              AND s.quantity >= :quantity
            """)
    int decreaseStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE Stock s
            SET s.quantity = s.quantity + :quantity,
                s.version = s.version + 1
            WHERE s.product.id = :productId
            """)
    int increaseStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );
}