package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Order;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@Transactional
public class OrderTotalSnapshotTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void orderResponseUsesStoredTotalAmount() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest orderRequest =
                new OrderRequest();

        orderRequest.setAddressId(1L);
        orderRequest.setItems(List.of(itemRequest));

        OrderResponse createdOrder =
                orderService.createOrder(user, orderRequest);

        BigDecimal expectedTotal =
                new BigDecimal("11.00");

        Assertions.assertEquals(
                expectedTotal,
                createdOrder.getOrderTotal()
        );

        Order storedOrder = orderDao
                .findById(createdOrder.getId())
                .orElseThrow();

        Assertions.assertEquals(
                expectedTotal,
                storedOrder.getTotalAmount(),
                "Order total must be stored in the database."
        );

        storedOrder.getQuantities()
                .iterator()
                .next()
                .setUnitPrice(new BigDecimal("99.99"));

        orderDao.saveAndFlush(storedOrder);

        entityManager.clear();

        OrderResponse retrievedOrder =
                orderService.getOrderById(
                        user,
                        createdOrder.getId()
                );

        Assertions.assertEquals(
                expectedTotal,
                retrievedOrder.getOrderTotal(),
                "Order response must use the stored total amount."
        );
    }
}