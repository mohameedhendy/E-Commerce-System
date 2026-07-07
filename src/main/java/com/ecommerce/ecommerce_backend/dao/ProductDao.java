package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductDao extends JpaRepository<Product, Long> {

    Page<Product> findAllByActiveTrue(Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(p.longDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    Page<Product> findAllByActive(Boolean active, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN p.stock s
        WHERE COALESCE(s.quantity, 0) <= :threshold
        """)
    Page<Product> findLowStockProducts(@Param("threshold") Integer threshold, Pageable pageable);

    long countByActive(Boolean active);

    @Query("""
        SELECT COUNT(p) FROM Product p
        LEFT JOIN p.stock s
        WHERE COALESCE(s.quantity, 0) <= :threshold
        """)
    long countLowStockProducts(@Param("threshold") Integer threshold);
}

