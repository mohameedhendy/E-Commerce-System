package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.CartDao;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
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

    private final CartDao cartDao;
    private final LocalUserDao localUserDao;
    private final ProductDao productDao;

    @Transactional(readOnly = true)
    public CartResponse getCart(LocalUser currentUser) {

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

        LocalUser user = localUserDao
                .findByIdForUpdate(
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User was not found"
                        )
                );

        Product product = productDao
                .findById(request.getProductId())
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

        int availableStock =
                product.getStock() == null
                        ? 0
                        : product.getStock()
                        .getQuantity();

        Cart cart = cartDao
                .findDetailedByUserId(user.getId())
                .orElseGet(() ->
                        createCart(user)
                );

        CartItem existingItem = cart
                .getItems()
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

        if (requestedTotalQuantity > availableStock) {
            throw new InsufficientStockException(
                    "Only "
                            + availableStock
                            + " items are available for product "
                            + product.getName()
            );
        }

        if (existingItem == null) {
            CartItem newItem = new CartItem();

            newItem.setProduct(product);
            newItem.setQuantity(
                    request.getQuantity()
            );

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

    private Cart createCart(LocalUser user) {

        Cart cart = new Cart();
        cart.setUser(user);

        return cart;
    }
}