package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class OrderProductNameSnapshotTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void orderKeepsOriginalProductNameAfterProductRename() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        String originalProductName =
                product.getName();

        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(
                product.getId()
        );
        itemRequest.setQuantity(1);

        OrderRequest orderRequest =
                new OrderRequest();

        orderRequest.setAddressId(1L);
        orderRequest.setItems(
                List.of(itemRequest)
        );

        OrderResponse createdOrder =
                orderService.createOrder(
                        user,
                        orderRequest
                );

        product.setName(
                "Renamed Product For Snapshot Test"
        );

        productDao.saveAndFlush(product);

        entityManager.flush();
        entityManager.clear();

        OrderResponse storedOrder =
                orderService.getOrderById(
                        user,
                        createdOrder.getId()
                );

        Assertions.assertEquals(
                originalProductName,
                storedOrder.getItems()
                        .getFirst()
                        .getProductName(),
                "Old orders must keep the product name used at purchase time."
        );

        Assertions.assertNotEquals(
                "Renamed Product For Snapshot Test",
                storedOrder.getItems()
                        .getFirst()
                        .getProductName()
        );
    }
}