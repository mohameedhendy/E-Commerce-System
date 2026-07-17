package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.CartDao;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
import com.ecommerce.ecommerce_backend.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.ecommerce_backend.exception.InsufficientStockException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
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

    @Test
    public void addingProductCreatesCartAndItem() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId())
        );

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(product.getId());
        request.setQuantity(2);

        CartResponse response =
                cartService.addItem(user, request);

        Assertions.assertNotNull(
                response.getId()
        );

        Assertions.assertEquals(
                1,
                response.getTotalItems()
        );

        Assertions.assertEquals(
                2,
                response.getTotalQuantity()
        );

        Assertions.assertEquals(
                product.getId(),
                response.getItems()
                        .getFirst()
                        .getProductId()
        );

        Assertions.assertEquals(
                2,
                response.getItems()
                        .getFirst()
                        .getQuantity()
        );

        Cart storedCart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseThrow();

        Assertions.assertEquals(
                1,
                storedCart.getItems().size()
        );

        Assertions.assertEquals(
                2,
                storedCart.getItems()
                        .getFirst()
                        .getQuantity()
        );
    }

    @Test
    public void addingSameProductIncreasesExistingQuantity() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        AddCartItemRequest firstRequest =
                new AddCartItemRequest();

        firstRequest.setProductId(product.getId());
        firstRequest.setQuantity(2);

        cartService.addItem(
                user,
                firstRequest
        );

        AddCartItemRequest secondRequest =
                new AddCartItemRequest();

        secondRequest.setProductId(product.getId());
        secondRequest.setQuantity(3);

        CartResponse response =
                cartService.addItem(
                        user,
                        secondRequest
                );

        Assertions.assertEquals(
                1,
                response.getTotalItems(),
                "The same product must not be duplicated."
        );

        Assertions.assertEquals(
                5,
                response.getTotalQuantity()
        );

        Assertions.assertEquals(
                5,
                response.getItems()
                        .getFirst()
                        .getQuantity()
        );

        Cart storedCart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseThrow();

        Assertions.assertEquals(
                1,
                storedCart.getItems().size()
        );
    }

    @Test
    public void quantityGreaterThanStockIsRejected() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        int availableStock =
                product.getStock().getQuantity();

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(product.getId());
        request.setQuantity(
                availableStock + 1
        );

        InsufficientStockException exception =
                Assertions.assertThrows(
                        InsufficientStockException.class,
                        () -> cartService.addItem(
                                user,
                                request
                        )
                );

        Assertions.assertTrue(
                exception.getMessage()
                        .contains(product.getName())
        );

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Rejected additions should not create an empty cart."
        );
    }

    @Test
    public void inactiveProductCannotBeAddedToCart() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        product.setActive(false);
        productDao.saveAndFlush(product);

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(product.getId());
        request.setQuantity(1);

        ResourceNotFoundException exception =
                Assertions.assertThrows(
                        ResourceNotFoundException.class,
                        () -> cartService.addItem(
                                user,
                                request
                        )
                );

        Assertions.assertEquals(
                "Product was not found",
                exception.getMessage()
        );

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Inactive products should not create a cart."
        );
    }

    @Test
    public void unknownProductCannotBeAddedToCart() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(Long.MAX_VALUE);
        request.setQuantity(1);

        ResourceNotFoundException exception =
                Assertions.assertThrows(
                        ResourceNotFoundException.class,
                        () -> cartService.addItem(
                                user,
                                request
                        )
                );

        Assertions.assertEquals(
                "Product was not found",
                exception.getMessage()
        );

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId())
        );
    }

    @Test
    public void cartItemQuantityCanBeUpdated() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        AddCartItemRequest addRequest =
                new AddCartItemRequest();

        addRequest.setProductId(product.getId());
        addRequest.setQuantity(1);

        CartResponse createdCart =
                cartService.addItem(user, addRequest);

        Long itemId = createdCart
                .getItems()
                .getFirst()
                .getId();

        UpdateCartItemQuantityRequest updateRequest =
                new UpdateCartItemQuantityRequest();

        updateRequest.setQuantity(3);

        CartResponse updatedCart =
                cartService.updateItemQuantity(
                        user,
                        itemId,
                        updateRequest
                );

        Assertions.assertEquals(
                1,
                updatedCart.getTotalItems()
        );

        Assertions.assertEquals(
                3,
                updatedCart.getTotalQuantity()
        );

        Assertions.assertEquals(
                3,
                updatedCart.getItems()
                        .getFirst()
                        .getQuantity()
        );
    }

    @Test
    public void updatedQuantityGreaterThanStockIsRejected() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        AddCartItemRequest addRequest =
                new AddCartItemRequest();

        addRequest.setProductId(product.getId());
        addRequest.setQuantity(1);

        CartResponse createdCart =
                cartService.addItem(user, addRequest);

        Long itemId = createdCart
                .getItems()
                .getFirst()
                .getId();

        UpdateCartItemQuantityRequest updateRequest =
                new UpdateCartItemQuantityRequest();

        updateRequest.setQuantity(
                product.getStock().getQuantity() + 1
        );

        Assertions.assertThrows(
                InsufficientStockException.class,
                () -> cartService.updateItemQuantity(
                        user,
                        itemId,
                        updateRequest
                )
        );
    }

    @Test
    public void cartItemCanBeRemoved() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(product.getId());
        request.setQuantity(2);

        CartResponse createdCart =
                cartService.addItem(user, request);

        Long cartId = createdCart.getId();

        Long itemId = createdCart
                .getItems()
                .getFirst()
                .getId();

        CartResponse updatedCart =
                cartService.removeItem(
                        user,
                        itemId
                );

        Assertions.assertEquals(
                cartId,
                updatedCart.getId(),
                "Removing the last item should keep the cart."
        );

        Assertions.assertTrue(
                updatedCart.getItems().isEmpty()
        );

        Assertions.assertEquals(
                0,
                updatedCart.getTotalItems()
        );

        Assertions.assertEquals(
                0,
                updatedCart.getTotalQuantity()
        );

        Assertions.assertEquals(
                new BigDecimal("0.00"),
                updatedCart.getSubtotal()
        );

        Cart storedCart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseThrow();

        Assertions.assertTrue(
                storedCart.getItems().isEmpty()
        );
    }

    @Test
    public void userCannotUpdateAnotherUsersCartItem() {

        LocalUser owner = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        LocalUser otherUser = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        AddCartItemRequest addRequest =
                new AddCartItemRequest();

        addRequest.setProductId(product.getId());
        addRequest.setQuantity(1);

        CartResponse ownerCart =
                cartService.addItem(owner, addRequest);

        Long itemId = ownerCart
                .getItems()
                .getFirst()
                .getId();

        UpdateCartItemQuantityRequest updateRequest =
                new UpdateCartItemQuantityRequest();

        updateRequest.setQuantity(2);

        ResourceNotFoundException exception =
                Assertions.assertThrows(
                        ResourceNotFoundException.class,
                        () -> cartService.updateItemQuantity(
                                otherUser,
                                itemId,
                                updateRequest
                        )
                );

        Assertions.assertEquals(
                "Cart item was not found",
                exception.getMessage()
        );
    }

    @Test
    public void userCannotRemoveAnotherUsersCartItem() {

        LocalUser owner = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        LocalUser otherUser = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product product = productDao
                .findById(1L)
                .orElseThrow();

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(product.getId());
        request.setQuantity(1);

        CartResponse ownerCart =
                cartService.addItem(owner, request);

        Long itemId = ownerCart
                .getItems()
                .getFirst()
                .getId();

        ResourceNotFoundException exception =
                Assertions.assertThrows(
                        ResourceNotFoundException.class,
                        () -> cartService.removeItem(
                                otherUser,
                                itemId
                        )
                );

        Assertions.assertEquals(
                "Cart item was not found",
                exception.getMessage()
        );
    }

    @Test
    public void cartCanBeClearedWithoutDeletingTheCart() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Product firstProduct = productDao
                .findById(1L)
                .orElseThrow();

        Product secondProduct = productDao
                .findById(2L)
                .orElseThrow();

        AddCartItemRequest firstRequest =
                new AddCartItemRequest();

        firstRequest.setProductId(
                firstProduct.getId()
        );
        firstRequest.setQuantity(2);

        CartResponse createdCart =
                cartService.addItem(
                        user,
                        firstRequest
                );

        AddCartItemRequest secondRequest =
                new AddCartItemRequest();

        secondRequest.setProductId(
                secondProduct.getId()
        );
        secondRequest.setQuantity(1);

        createdCart = cartService.addItem(
                user,
                secondRequest
        );

        Long cartId = createdCart.getId();

        Assertions.assertEquals(
                2,
                createdCart.getTotalItems()
        );

        CartResponse clearedCart =
                cartService.clearCart(user);

        Assertions.assertEquals(
                cartId,
                clearedCart.getId(),
                "Clearing items should not delete the cart."
        );

        Assertions.assertTrue(
                clearedCart.getItems().isEmpty()
        );

        Assertions.assertEquals(
                0,
                clearedCart.getTotalItems()
        );

        Assertions.assertEquals(
                0,
                clearedCart.getTotalQuantity()
        );

        Assertions.assertEquals(
                new BigDecimal("0.00"),
                clearedCart.getSubtotal()
        );

        Cart storedCart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseThrow();

        Assertions.assertEquals(
                cartId,
                storedCart.getId()
        );

        Assertions.assertTrue(
                storedCart.getItems().isEmpty(),
                "Cart items should be deleted from the database."
        );
    }

    @Test
    public void clearingMissingCartReturnsEmptyResponseWithoutCreatingCart() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserC")
                .orElseThrow();

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Test user should not already have a cart."
        );

        CartResponse response =
                cartService.clearCart(user);

        Assertions.assertNull(
                response.getId()
        );

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

        Assertions.assertFalse(
                cartDao.existsByUser_Id(user.getId()),
                "Clearing a missing cart should not create one."
        );
    }
}