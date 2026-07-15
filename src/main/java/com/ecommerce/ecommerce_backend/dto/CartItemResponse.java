package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;

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