package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dto.AdminOrderStatusRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class OrderStatusTransitionTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void adminCanConfirmThenCancelOrder() {

        int stockBefore = getStockQuantity(1L);

        OrderResponse confirmedOrder =
                orderService.updateOrderStatus(
                        1L,
                        statusRequest("CONFIRMED")
                );

        Assertions.assertEquals(
                "CONFIRMED",
                confirmedOrder.getStatus()
        );

        Assertions.assertEquals(
                stockBefore,
                getStockQuantity(1L),
                "Confirming an order must not change stock."
        );

        OrderResponse cancelledOrder =
                orderService.updateOrderStatus(
                        1L,
                        statusRequest("CANCELLED")
                );

        Assertions.assertEquals(
                "CANCELLED",
                cancelledOrder.getStatus()
        );

        int orderedQuantity =
                getOrderItemQuantity(1L, 1L);

        Assertions.assertEquals(
                stockBefore + orderedQuantity,
                getStockQuantity(1L),
                "Cancelling must restore ordered stock."
        );
    }

    @Test
    public void cancellingOrderTwiceDoesNotRestoreStockTwice() {

        orderService.updateOrderStatus(
                1L,
                statusRequest("CANCELLED")
        );

        int stockAfterFirstCancellation =
                getStockQuantity(1L);

        Assertions.assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        statusRequest("CANCELLED")
                )
        );

        Assertions.assertEquals(
                stockAfterFirstCancellation,
                getStockQuantity(1L),
                "Repeated cancellation must not restore stock twice."
        );
    }

    @Test
    public void customerCannotCancelConfirmedOrder() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        orderService.updateOrderStatus(
                2L,
                statusRequest("CONFIRMED")
        );

        int stockBefore =
                getStockQuantity(3L);

        Assertions.assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.cancelOrder(
                        user,
                        2L
                )
        );

        Assertions.assertEquals(
                stockBefore,
                getStockQuantity(3L),
                "Rejected cancellation must not modify stock."
        );
    }

    @Test
    public void cancelledOrderCannotBeConfirmed() {

        orderService.updateOrderStatus(
                3L,
                statusRequest("CANCELLED")
        );

        Assertions.assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateOrderStatus(
                        3L,
                        statusRequest("CONFIRMED")
                )
        );
    }

    private AdminOrderStatusRequest statusRequest(
            String status) {

        AdminOrderStatusRequest request =
                new AdminOrderStatusRequest();

        request.setStatus(status);

        return request;
    }

    private int getStockQuantity(Long productId) {

        Number result = (Number) entityManager
                .createNativeQuery("""
                        SELECT quantity
                        FROM stock
                        WHERE product_id = :productId
                        """)
                .setParameter("productId", productId)
                .getSingleResult();

        return result.intValue();
    }

    private int getOrderItemQuantity(
            Long orderId,
            Long productId) {

        Number result = (Number) entityManager
                .createNativeQuery("""
                        SELECT quantity
                        FROM product_order_quantity
                        WHERE order_id = :orderId
                          AND product_id = :productId
                        """)
                .setParameter("orderId", orderId)
                .setParameter("productId", productId)
                .getSingleResult();

        return result.intValue();
    }
}