package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import com.ecommerce.ecommerce_backend.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class OrderCancellationConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private ProductDao productDao;

    @Test
    public void concurrentCancellationRestoresStockOnlyOnce()
            throws Exception {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        Long productId = product.getId();

        int stockBeforeOrder =
                product.getStock().getQuantity();

        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(productId);
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

        Long orderId = createdOrder.getId();

        int stockAfterOrder = productDao
                .findById(productId)
                .orElseThrow()
                .getStock()
                .getQuantity();

        Assertions.assertEquals(
                stockBeforeOrder - 1,
                stockAfterOrder
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Callable<Boolean> cancellationTask = () -> {

            LocalUser threadUser = localUserDao
                    .findByUsernameIgnoreCase("UserA")
                    .orElseThrow();

            readyLatch.countDown();

            if (!startLatch.await(
                    5,
                    TimeUnit.SECONDS
            )) {
                throw new IllegalStateException(
                        "Concurrent tasks did not start in time"
                );
            }

            try {
                orderService.cancelOrder(
                        threadUser,
                        orderId
                );

                return true;

            } catch (InvalidOrderStatusException exception) {
                return false;
            }
        };

        try {
            Future<Boolean> firstCancellation =
                    executor.submit(cancellationTask);

            Future<Boolean> secondCancellation =
                    executor.submit(cancellationTask);

            Assertions.assertTrue(
                    readyLatch.await(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "Both cancellation requests should be ready."
            );

            startLatch.countDown();

            boolean firstSucceeded =
                    firstCancellation.get(
                            10,
                            TimeUnit.SECONDS
                    );

            boolean secondSucceeded =
                    secondCancellation.get(
                            10,
                            TimeUnit.SECONDS
                    );

            Assertions.assertNotEquals(
                    firstSucceeded,
                    secondSucceeded,
                    "Exactly one cancellation request should succeed."
            );

            Order storedOrder = orderDao
                    .findById(orderId)
                    .orElseThrow();

            Assertions.assertEquals(
                    OrderStatus.CANCELLED,
                    storedOrder.getStatus()
            );

            int stockAfterCancellation = productDao
                    .findById(productId)
                    .orElseThrow()
                    .getStock()
                    .getQuantity();

            Assertions.assertEquals(
                    stockBeforeOrder,
                    stockAfterCancellation,
                    "Stock must be restored exactly once."
            );

        } finally {
            executor.shutdownNow();

            if (orderDao.existsById(orderId)) {
                orderDao.deleteById(orderId);
            }
        }
    }
}