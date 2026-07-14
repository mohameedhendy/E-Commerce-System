package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.ProductOrderQuantity;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal itemTotal;

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