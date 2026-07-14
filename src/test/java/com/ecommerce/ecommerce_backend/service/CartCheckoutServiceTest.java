package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.CartDao;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_backend.dto.CartCheckoutRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class CartCheckoutServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private CartDao cartDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void checkoutCreatesOrderDecreasesStockAndClearsCart() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        Long productId = product.getId();

        int stockBeforeCheckout =
                product.getStock().getQuantity();

        long ordersBeforeCheckout =
                orderDao.count();

        AddCartItemRequest addRequest =
                new AddCartItemRequest();

        addRequest.setProductId(productId);
        addRequest.setQuantity(2);

        cartService.addItem(
                user,
                addRequest
        );

        CartCheckoutRequest checkoutRequest =
                new CartCheckoutRequest();

        checkoutRequest.setAddressId(1L);

        OrderResponse response =
                cartService.checkout(
                        user,
                        checkoutRequest
                );

        Assertions.assertNotNull(response);

        entityManager.flush();
        entityManager.clear();

        Assertions.assertEquals(
                ordersBeforeCheckout + 1,
                orderDao.count(),
                "Checkout should create exactly one order."
        );

        Cart storedCart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseThrow();

        Assertions.assertTrue(
                storedCart.getItems().isEmpty(),
                "The cart should be empty after checkout."
        );

        Product updatedProduct = productDao
                .findById(productId)
                .orElseThrow();

        Assertions.assertEquals(
                stockBeforeCheckout - 2,
                updatedProduct.getStock().getQuantity(),
                "Product stock should decrease by the ordered quantity."
        );
    }

    @Test
    public void emptyCartCannotBeCheckedOut() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        long ordersBeforeCheckout =
                orderDao.count();

        CartCheckoutRequest request =
                new CartCheckoutRequest();

        request.setAddressId(1L);

        InvalidOrderStatusException exception =
                Assertions.assertThrows(
                        InvalidOrderStatusException.class,
                        () -> cartService.checkout(
                                user,
                                request
                        )
                );

        Assertions.assertEquals(
                "Cart is empty",
                exception.getMessage()
        );

        Assertions.assertEquals(
                ordersBeforeCheckout,
                orderDao.count(),
                "An empty cart must not create an order."
        );

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Failed checkout should not create a cart."
        );
    }
}