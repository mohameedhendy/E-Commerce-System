package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class InactiveProductOrderTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private ProductDao productDao;

    @Test
    public void inactiveProductCannotBeOrdered() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        product.setActive(false);
        productDao.saveAndFlush(product);

        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(product.getId());
        itemRequest.setQuantity(1);

        OrderRequest orderRequest =
                new OrderRequest();

        orderRequest.setAddressId(1L);
        orderRequest.setItems(
                List.of(itemRequest)
        );

        ResourceNotFoundException exception =
                Assertions.assertThrows(
                        ResourceNotFoundException.class,
                        () -> orderService.createOrder(
                                user,
                                orderRequest
                        )
                );

        Assertions.assertEquals(
                "Product was not found",
                exception.getMessage()
        );
    }
}