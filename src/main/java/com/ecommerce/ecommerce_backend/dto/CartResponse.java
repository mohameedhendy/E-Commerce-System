package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CartResponse {

    private Long id;
    private List<CartItemResponse> items;
    private int totalItems;
    private int totalQuantity;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;

    private CartResponse() {
    }

    public CartResponse(
            Cart cart
    ) {

        this.id =
                cart.getId();

        this.items =
                cart.getItems()
                        .stream()
                        .map(CartItemResponse::new)
                        .toList();

        this.totalItems =
                items.size();

        this.totalQuantity =
                items.stream()
                        .mapToInt(
                                CartItemResponse::getQuantity
                        )
                        .sum();

        BigDecimal calculatedSubtotal =
                items.stream()
                        .map(
                                CartItemResponse::getItemTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        this.subtotal =
                MoneyUtils.scale(
                        calculatedSubtotal
                );

        this.createdAt =
                cart.getCreatedAt();
    }

    public static CartResponse empty() {

        CartResponse response =
                new CartResponse();

        response.id = null;
        response.items = List.of();
        response.totalItems = 0;
        response.totalQuantity = 0;

        response.subtotal =
                MoneyUtils.scale(
                        BigDecimal.ZERO
                );

        response.createdAt = null;

        return response;
    }
}