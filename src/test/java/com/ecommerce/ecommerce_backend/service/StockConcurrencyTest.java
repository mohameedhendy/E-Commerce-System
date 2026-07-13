package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.exception.InsufficientStockException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stock_concurrency_test"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StockConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void onlyOneConcurrentOrderCanBuyLastItem()
            throws Exception {

        setProductStockToOne();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Callable<Boolean> placeOrderTask = () -> {
            readyLatch.countDown();

            boolean started = startLatch.await(
                    5,
                    TimeUnit.SECONDS
            );

            if (!started) {
                throw new IllegalStateException(
                        "Concurrent test did not start in time"
                );
            }

            LocalUser user = localUserDao
                    .findByUsernameIgnoreCase("UserA")
                    .orElseThrow();

            try {
                orderService.createOrder(
                        user,
                        createOrderRequest()
                );

                return true;
            } catch (InsufficientStockException ex) {
                return false;
            }
        };

        try {
            Future<Boolean> firstResult =
                    executor.submit(placeOrderTask);

            Future<Boolean> secondResult =
                    executor.submit(placeOrderTask);

            Assertions.assertTrue(
                    readyLatch.await(5, TimeUnit.SECONDS),
                    "Both requests must be ready."
            );

            startLatch.countDown();

            boolean firstSucceeded =
                    firstResult.get(15, TimeUnit.SECONDS);

            boolean secondSucceeded =
                    secondResult.get(15, TimeUnit.SECONDS);

            long successfulOrders = List.of(
                            firstSucceeded,
                            secondSucceeded
                    )
                    .stream()
                    .filter(Boolean::booleanValue)
                    .count();

            Assertions.assertEquals(
                    1,
                    successfulOrders,
                    "Only one request may purchase the last item."
            );

            Assertions.assertEquals(
                    0,
                    getCurrentStock(),
                    "Stock must finish at zero, not below zero."
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private void setProductStockToOne() {
        transactionTemplate.executeWithoutResult(status -> {
            Product product = productDao
                    .findById(1L)
                    .orElseThrow();

            product.getStock().setQuantity(1);

            productDao.saveAndFlush(product);
        });
    }

    private Integer getCurrentStock() {
        return transactionTemplate.execute(status ->
                productDao
                        .findById(1L)
                        .orElseThrow()
                        .getStock()
                        .getQuantity()
        );
    }

    private OrderRequest createOrderRequest() {
        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);

        OrderRequest orderRequest =
                new OrderRequest();

        orderRequest.setAddressId(1L);
        orderRequest.setItems(List.of(itemRequest));

        return orderRequest;
    }
}