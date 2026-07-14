package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.CartDao;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest
@Transactional
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartDao cartDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private ProductDao productDao;

    @Test
    public void userWithoutCartReceivesEmptyCart() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Test user should not already have a cart."
        );

        CartResponse response =
                cartService.getCart(user);

        Assertions.assertNull(response.getId());

        Assertions.assertTrue(
                response.getItems().isEmpty()
        );

        Assertions.assertEquals(
                0,
                response.getTotalItems()
        );

        Assertions.assertEquals(
                0,
                response.getTotalQuantity()
        );

        Assertions.assertEquals(
                new BigDecimal("0.00"),
                response.getSubtotal()
        );

        Assertions.assertNull(
                response.getCreatedAt()
        );

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Reading an empty cart should not create a database row."
        );
    }

    @Test
    public void existingCartIsReturnedWithCalculatedTotals() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product firstProduct = productDao
                .findById(1L)
                .orElseThrow();

        Product secondProduct = productDao
                .findById(2L)
                .orElseThrow();

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem firstItem = new CartItem();
        firstItem.setProduct(firstProduct);
        firstItem.setQuantity(2);

        CartItem secondItem = new CartItem();
        secondItem.setProduct(secondProduct);
        secondItem.setQuantity(3);

        cart.addItem(firstItem);
        cart.addItem(secondItem);

        Cart savedCart =
                cartDao.saveAndFlush(cart);

        CartResponse response =
                cartService.getCart(user);

        BigDecimal expectedSubtotal =
                firstProduct.getPrice()
                        .multiply(
                                BigDecimal.valueOf(2)
                        )
                        .add(
                                secondProduct.getPrice()
                                        .multiply(
                                                BigDecimal.valueOf(3)
                                        )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        Assertions.assertEquals(
                savedCart.getId(),
                response.getId()
        );

        Assertions.assertEquals(
                2,
                response.getTotalItems()
        );

        Assertions.assertEquals(
                5,
                response.getTotalQuantity()
        );

        Assertions.assertEquals(
                expectedSubtotal,
                response.getSubtotal()
        );

        Assertions.assertEquals(
                2,
                response.getItems().size()
        );

        Assertions.assertNotNull(
                response.getCreatedAt()
        );
    }

    @Test
    public void cartResponseIncludesCurrentProductInformation() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        cart.addItem(item);

        cartDao.saveAndFlush(cart);

        CartResponse response =
                cartService.getCart(user);

        Assertions.assertEquals(
                product.getId(),
                response.getItems()
                        .getFirst()
                        .getProductId()
        );

        Assertions.assertEquals(
                product.getName(),
                response.getItems()
                        .getFirst()
                        .getProductName()
        );

        Assertions.assertEquals(
                product.getPrice()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        ),
                response.getItems()
                        .getFirst()
                        .getUnitPrice()
        );

        Assertions.assertEquals(
                product.getStock().getQuantity(),
                response.getItems()
                        .getFirst()
                        .getAvailableStock()
        );

        Assertions.assertEquals(
                Boolean.TRUE.equals(
                        product.getActive()
                ),
                response.getItems()
                        .getFirst()
                        .isActive()
        );
    }
}