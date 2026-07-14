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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CART_ITEM_NOT_FOUND =
            "Cart item was not found";

    private final CartDao cartDao;
    private final LocalUserDao localUserDao;
    private final ProductDao productDao;

    @Transactional(readOnly = true)
    public CartResponse getCart(LocalUser currentUser) {

        return cartDao
                .findDetailedByUserId(currentUser.getId())
                .map(CartResponse::new)
                .orElseGet(CartResponse::empty);
    }

    @Transactional
    public CartResponse addItem(
            LocalUser currentUser,
            AddCartItemRequest request
    ) {

        LocalUser user = lockUser(currentUser.getId());

        Product product =
                getActiveProduct(request.getProductId());

        Cart cart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseGet(() -> createCart(user));

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getProduct()
                                .getId()
                                .equals(product.getId())
                )
                .findFirst()
                .orElse(null);

        int currentQuantity =
                existingItem == null
                        ? 0
                        : existingItem.getQuantity();

        long requestedTotalQuantity =
                (long) currentQuantity
                        + request.getQuantity();

        validateAvailableStock(
                product,
                requestedTotalQuantity
        );

        if (existingItem == null) {
            CartItem newItem = new CartItem();

            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());

            cart.addItem(newItem);
        } else {
            existingItem.setQuantity(
                    Math.toIntExact(
                            requestedTotalQuantity
                    )
            );
        }

        Cart savedCart =
                cartDao.saveAndFlush(cart);

        return new CartResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItemQuantity(
            LocalUser currentUser,
            Long itemId,
            UpdateCartItemQuantityRequest request
    ) {

        LocalUser user = lockUser(currentUser.getId());

        Cart cart = getCartOrThrow(user.getId());

        CartItem item = getOwnedItemOrThrow(
                cart,
                itemId
        );

        Product product = item.getProduct();

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException(
                    "Product was not found"
            );
        }

        validateAvailableStock(
                product,
                request.getQuantity()
        );

        item.setQuantity(request.getQuantity());

        Cart savedCart =
                cartDao.saveAndFlush(cart);

        return new CartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeItem(
            LocalUser currentUser,
            Long itemId
    ) {

        LocalUser user = lockUser(currentUser.getId());

        Cart cart = getCartOrThrow(user.getId());

        CartItem item = getOwnedItemOrThrow(
                cart,
                itemId
        );

        cart.removeItem(item);

        Cart savedCart =
                cartDao.saveAndFlush(cart);

        return new CartResponse(savedCart);
    }

    @Transactional
    public CartResponse clearCart(LocalUser currentUser) {

        LocalUser user = lockUser(
                currentUser.getId()
        );

        return cartDao
                .findDetailedByUserId(user.getId())
                .map(cart -> {

                    cart.clearItems();

                    Cart savedCart =
                            cartDao.saveAndFlush(cart);

                    return new CartResponse(savedCart);
                })
                .orElseGet(CartResponse::empty);
    }

    private LocalUser lockUser(Long userId) {

        return localUserDao
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User was not found"
                        )
                );
    }

    private Product getActiveProduct(Long productId) {

        Product product = productDao
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product was not found"
                        )
                );

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException(
                    "Product was not found"
            );
        }

        return product;
    }

    private Cart getCartOrThrow(Long userId) {

        return cartDao
                .findDetailedByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CART_ITEM_NOT_FOUND
                        )
                );
    }

    private CartItem getOwnedItemOrThrow(
            Cart cart,
            Long itemId
    ) {

        return cart.getItems()
                .stream()
                .filter(item ->
                        item.getId().equals(itemId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CART_ITEM_NOT_FOUND
                        )
                );
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

    private Cart createCart(LocalUser user) {

        Cart cart = new Cart();
        cart.setUser(user);

        return cart;
    }
}