package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartItemResponse {

    private final Long id;
    private final Long productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final Integer quantity;
    private final BigDecimal itemTotal;
    private final Integer availableStock;
    private final boolean active;

    public CartItemResponse(
            CartItem item
    ) {

        Product product =
                item.getProduct();

        this.id =
                item.getId();

        this.productId =
                product.getId();

        this.productName =
                product.getName();

        this.unitPrice =
                MoneyUtils.scale(
                        product.getPrice()
                );

        this.quantity =
                item.getQuantity();

        this.itemTotal =
                MoneyUtils.calculateTotal(
                        unitPrice,
                        quantity
                );

        this.availableStock =
                product.getStock() == null
                        ? 0
                        : product.getStock()
                        .getQuantity();

        this.active =
                Boolean.TRUE.equals(
                        product.getActive()
                );
    }
}