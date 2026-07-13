package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
@Transactional
public class ProductPricePrecisionTest {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void productPriceIsStoredWithExactDecimalPrecision() {

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        product.setPrice(new BigDecimal("0.10"));

        productDao.saveAndFlush(product);
        entityManager.clear();

        Product storedProduct = productDao
                .findById(1L)
                .orElseThrow();

        Assertions.assertEquals(
                0,
                new BigDecimal("0.10")
                        .compareTo(storedProduct.getPrice()),
                "Product price must be stored exactly."
        );

        Assertions.assertEquals(
                2,
                storedProduct.getPrice().scale(),
                "Product price must use two decimal places."
        );

        BigDecimal total =
                storedProduct.getPrice()
                        .multiply(BigDecimal.valueOf(3));

        Assertions.assertEquals(
                new BigDecimal("0.30"),
                total,
                "Money calculations must not contain floating-point errors."
        );
    }
}