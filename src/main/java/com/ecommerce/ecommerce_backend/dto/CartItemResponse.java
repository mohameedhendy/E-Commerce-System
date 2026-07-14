package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal itemTotal;
    private Integer availableStock;
    private boolean active;

    public CartItemResponse(CartItem item) {

        Product product = item.getProduct();

        this.id = item.getId();
        this.productId = product.getId();
        this.productName = product.getName();

        this.unitPrice = product.getPrice()
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        this.quantity = item.getQuantity();

        this.itemTotal = unitPrice
                .multiply(
                        BigDecimal.valueOf(quantity)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        this.availableStock =
                product.getStock() != null
                        ? product.getStock().getQuantity()
                        : 0;

        this.active =
                Boolean.TRUE.equals(
                        product.getActive()
                );
    }
}