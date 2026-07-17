package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.CartDao;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.*;
import com.ecommerce.ecommerce_backend.exception.InsufficientStockException;
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String USER_NOT_FOUND =
            "User was not found";

    private static final String PRODUCT_NOT_FOUND =
            "Product was not found";

    private static final String CART_ITEM_NOT_FOUND =
            "Cart item was not found";

    private static final String CART_NOT_FOUND =
            "Cart was not found";

    private static final String CART_EMPTY =
            "Cart is empty";

    private final CartDao cartDao;
    private final LocalUserDao localUserDao;
    private final ProductDao productDao;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public CartResponse getCart(
            LocalUser currentUser
    ) {

        return cartDao
                .findDetailedByUserId(
                        currentUser.getId()
                )
                .map(CartResponse::new)
                .orElseGet(CartResponse::empty);
    }

    @Transactional
    public CartResponse addItem(
            LocalUser currentUser,
            AddCartItemRequest request
    ) {

        LocalUser user =
                lockUser(currentUser.getId());

        Product product =
                getActiveProduct(
                        request.getProductId()
                );

        Cart cart =
                getOrCreateCart(user);

        Optional<CartItem> existingItem =
                findItemByProductId(
                        cart,
                        product.getId()
                );

        long requestedTotalQuantity =
                calculateRequestedTotalQuantity(
                        existingItem,
                        request.getQuantity()
                );

        validateAvailableStock(
                product,
                requestedTotalQuantity
        );

        addOrIncreaseItem(
                cart,
                existingItem,
                product,
                request.getQuantity(),
                requestedTotalQuantity
        );

        return saveAndConvert(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(
            LocalUser currentUser,
            Long itemId,
            UpdateCartItemQuantityRequest request
    ) {

        LocalUser user =
                lockUser(currentUser.getId());

        Cart cart =
                getCartForItemOperationOrThrow(
                        user.getId()
                );

        CartItem item =
                getOwnedItemOrThrow(
                        cart,
                        itemId
                );

        Product product =
                item.getProduct();

        validateActiveProduct(product);

        validateAvailableStock(
                product,
                request.getQuantity()
        );

        item.setQuantity(
                request.getQuantity()
        );

        return saveAndConvert(cart);
    }

    @Transactional
    public CartResponse removeItem(
            LocalUser currentUser,
            Long itemId
    ) {

        LocalUser user =
                lockUser(currentUser.getId());

        Cart cart =
                getCartForItemOperationOrThrow(
                        user.getId()
                );

        CartItem item =
                getOwnedItemOrThrow(
                        cart,
                        itemId
                );

        cart.removeItem(item);

        return saveAndConvert(cart);
    }

    @Transactional
    public CartResponse clearCart(
            LocalUser currentUser
    ) {

        LocalUser user =
                lockUser(currentUser.getId());

        return cartDao
                .findDetailedByUserId(
                        user.getId()
                )
                .map(this::clearAndConvert)
                .orElseGet(CartResponse::empty);
    }

    @Transactional
    public OrderResponse checkout(
            LocalUser currentUser,
            CartCheckoutRequest request
    ) {

        LocalUser user =
                lockUser(currentUser.getId());

        Cart cart =
                getNonEmptyCartForCheckout(
                        user.getId()
                );

        OrderRequest orderRequest =
                createOrderRequest(
                        cart,
                        request.getAddressId()
                );

        OrderResponse orderResponse =
                orderService.createOrder(
                        user,
                        orderRequest
                );

        clearCartAfterCheckout(
                user.getId()
        );

        return orderResponse;
    }

    private LocalUser lockUser(
            Long userId
    ) {

        return localUserDao
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                USER_NOT_FOUND
                        )
                );
    }

    private Product getActiveProduct(
            Long productId
    ) {

        Product product = productDao
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PRODUCT_NOT_FOUND
                        )
                );

        validateActiveProduct(product);

        return product;
    }

    private void validateActiveProduct(
            Product product
    ) {

        if (!Boolean.TRUE.equals(
                product.getActive()
        )) {
            throw new ResourceNotFoundException(
                    PRODUCT_NOT_FOUND
            );
        }
    }

    private Cart getOrCreateCart(
            LocalUser user
    ) {

        return cartDao
                .findDetailedByUserId(
                        user.getId()
                )
                .orElseGet(() ->
                        createCart(user)
                );
    }

    private Cart createCart(
            LocalUser user
    ) {

        Cart cart = new Cart();
        cart.setUser(user);

        return cart;
    }

    private Cart getCartForItemOperationOrThrow(
            Long userId
    ) {

        return cartDao
                .findDetailedByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CART_ITEM_NOT_FOUND
                        )
                );
    }

    private Cart getNonEmptyCartForCheckout(
            Long userId
    ) {

        Cart cart = cartDao
                .findDetailedByUserId(userId)
                .orElseThrow(() ->
                        new InvalidOrderStatusException(
                                CART_EMPTY
                        )
                );

        if (cart.getItems().isEmpty()) {
            throw new InvalidOrderStatusException(
                    CART_EMPTY
            );
        }

        return cart;
    }

    private Optional<CartItem> findItemByProductId(
            Cart cart,
            Long productId
    ) {

        return cart.getItems()
                .stream()
                .filter(item ->
                        item.getProduct()
                                .getId()
                                .equals(productId)
                )
                .findFirst();
    }

    private CartItem getOwnedItemOrThrow(
            Cart cart,
            Long itemId
    ) {

        return cart.getItems()
                .stream()
                .filter(item ->
                        item.getId()
                                .equals(itemId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CART_ITEM_NOT_FOUND
                        )
                );
    }

    private long calculateRequestedTotalQuantity(
            Optional<CartItem> existingItem,
            Integer quantityToAdd
    ) {

        int currentQuantity =
                existingItem
                        .map(CartItem::getQuantity)
                        .orElse(0);

        return (long) currentQuantity
                + quantityToAdd;
    }

    private void addOrIncreaseItem(
            Cart cart,
            Optional<CartItem> existingItem,
            Product product,
            Integer quantityToAdd,
            long requestedTotalQuantity
    ) {

        if (existingItem.isPresent()) {
            existingItem
                    .get()
                    .setQuantity(
                            Math.toIntExact(
                                    requestedTotalQuantity
                            )
                    );

            return;
        }

        CartItem newItem =
                new CartItem();

        newItem.setProduct(product);
        newItem.setQuantity(quantityToAdd);

        cart.addItem(newItem);
    }

    private void validateAvailableStock(
            Product product,
            long requestedQuantity
    ) {

        int availableStock =
                product.getStock() == null
                        ? 0
                        : product.getStock()
                        .getQuantity();

        if (requestedQuantity > availableStock) {
            throw new InsufficientStockException(
                    "Only "
                            + availableStock
                            + " items are available for product "
                            + product.getName()
            );
        }
    }

    private CartResponse saveAndConvert(
            Cart cart
    ) {

        Cart savedCart =
                cartDao.saveAndFlush(cart);

        return new CartResponse(savedCart);
    }

    private CartResponse clearAndConvert(
            Cart cart
    ) {

        cart.clearItems();

        return saveAndConvert(cart);
    }

    private OrderRequest createOrderRequest(
            Cart cart,
            Long addressId
    ) {

        OrderRequest orderRequest =
                new OrderRequest();

        orderRequest.setAddressId(addressId);

        orderRequest.setItems(
                cart.getItems()
                        .stream()
                        .map(this::createOrderItemRequest)
                        .toList()
        );

        return orderRequest;
    }

    private OrderItemRequest createOrderItemRequest(
            CartItem cartItem
    ) {

        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(
                cartItem.getProduct()
                        .getId()
        );

        itemRequest.setQuantity(
                cartItem.getQuantity()
        );

        return itemRequest;
    }

    private void clearCartAfterCheckout(
            Long userId
    ) {

        /*
         * Order creation updates stock using database-level
         * update queries. Reload the cart before clearing it
         * to keep the persistence context consistent.
         */
        Cart cartToClear = cartDao
                .findDetailedByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CART_NOT_FOUND
                        )
                );

        cartToClear.clearItems();

        cartDao.saveAndFlush(cartToClear);
    }
}