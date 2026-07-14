package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Cart;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public CartResponse(Cart cart) {

        this.id = cart.getId();

        this.items = cart.getItems()
                .stream()
                .map(CartItemResponse::new)
                .toList();

        this.totalItems = items.size();

        this.totalQuantity = items
                .stream()
                .mapToInt(
                        CartItemResponse::getQuantity
                )
                .sum();

        this.subtotal = items
                .stream()
                .map(
                        CartItemResponse::getItemTotal
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        this.createdAt = cart.getCreatedAt();
    }
}