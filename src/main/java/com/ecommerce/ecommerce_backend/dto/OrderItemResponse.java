package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.ProductOrderQuantity;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal itemTotal;

    public OrderItemResponse(ProductOrderQuantity item) {
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();

        this.price = item.getUnitPrice()
                .setScale(2, RoundingMode.HALF_UP);

        this.quantity = item.getQuantity();

        this.itemTotal = this.price
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }

}