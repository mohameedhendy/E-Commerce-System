package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@Transactional
public class OrderPriceSnapshotTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private ProductDao productDao;

    @Test
    public void orderKeepsOriginalPriceWhenProductPriceChanges() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setAddressId(1L);
        orderRequest.setItems(List.of(itemRequest));

        OrderResponse createdOrder =
                orderService.createOrder(user, orderRequest);

        Assertions.assertEquals(
                new BigDecimal("5.50"),
                createdOrder.getItems().get(0).getPrice()
        );

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        product.setPrice(99.99);
        productDao.saveAndFlush(product);

        OrderResponse storedOrder =
                orderService.getOrderById(
                        user,
                        createdOrder.getId()
                );

        Assertions.assertEquals(
                new BigDecimal("5.50"),
                storedOrder.getItems().get(0).getPrice(),
                "Old order must keep the original product price."
        );

        Assertions.assertEquals(
                new BigDecimal("5.50"),
                storedOrder.getOrderTotal(),
                "Order total must use the stored price snapshot."
        );
    }
}