package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
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
public class CartRepositoryTest {

    @Autowired
    private CartDao cartDao;

    @Autowired
    private CartItemDao cartItemDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private ProductDao productDao;

    @Test
    public void cartCanBeSavedAndFoundByUserId() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Cart cart = new Cart();
        cart.setUser(user);

        Cart savedCart = cartDao.saveAndFlush(cart);

        Assertions.assertNotNull(
                savedCart.getId(),
                "Cart ID should be generated."
        );

        Cart foundCart = cartDao
                .findByUser_Id(user.getId())
                .orElseThrow();

        Assertions.assertEquals(
                savedCart.getId(),
                foundCart.getId()
        );

        Assertions.assertEquals(
                user.getId(),
                foundCart.getUser().getId()
        );

        Assertions.assertTrue(
                cartDao.existsByUser_Id(user.getId())
        );
    }

    @Test
    public void cartItemCanBeSavedAndFoundByCartAndProduct() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        Cart cart = new Cart();
        cart.setUser(user);

        Cart savedCart = cartDao.saveAndFlush(cart);

        CartItem item = new CartItem();
        item.setCart(savedCart);
        item.setProduct(product);
        item.setQuantity(2);

        CartItem savedItem =
                cartItemDao.saveAndFlush(item);

        Assertions.assertNotNull(
                savedItem.getId(),
                "Cart item ID should be generated."
        );

        CartItem foundItem = cartItemDao
                .findByCart_IdAndProduct_Id(
                        savedCart.getId(),
                        product.getId()
                )
                .orElseThrow();

        Assertions.assertEquals(
                product.getId(),
                foundItem.getProduct().getId()
        );

        Assertions.assertEquals(
                2,
                foundItem.getQuantity()
        );

        Assertions.assertEquals(
                1,
                cartItemDao.countByCart_Id(
                        savedCart.getId()
                )
        );
    }

    @Test
    public void cartItemsAreReturnedInAscendingIdOrder() {

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

        Cart savedCart = cartDao.saveAndFlush(cart);

        CartItem firstItem = new CartItem();
        firstItem.setCart(savedCart);
        firstItem.setProduct(firstProduct);
        firstItem.setQuantity(1);

        CartItem secondItem = new CartItem();
        secondItem.setCart(savedCart);
        secondItem.setProduct(secondProduct);
        secondItem.setQuantity(3);

        cartItemDao.saveAndFlush(firstItem);
        cartItemDao.saveAndFlush(secondItem);

        List<CartItem> items =
                cartItemDao.findAllByCart_IdOrderByIdAsc(
                        savedCart.getId()
                );

        Assertions.assertEquals(
                2,
                items.size()
        );

        Assertions.assertTrue(
                items.get(0).getId()
                        < items.get(1).getId(),
                "Cart items should be ordered by ID."
        );
    }
}