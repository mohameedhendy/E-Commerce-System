package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.ProductOrderQuantity;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemResponse {

    private final Long productId;
    private final String productName;
    private final BigDecimal price;
    private final Integer quantity;
    private final BigDecimal itemTotal;

    public OrderItemResponse(
            ProductOrderQuantity item
    ) {

        this.productId =
                item.getProduct().getId();

        this.productName =
                item.getProductName();

        this.price = MoneyUtils.scale(
                item.getUnitPrice()
        );

        this.quantity =
                item.getQuantity();

        this.itemTotal =
                MoneyUtils.calculateTotal(
                        this.price,
                        this.quantity
                );
    }
}