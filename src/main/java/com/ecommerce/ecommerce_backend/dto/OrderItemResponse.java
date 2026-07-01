package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.ProductOrderQuantity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderItemResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal itemTotal;

    public OrderItemResponse(ProductOrderQuantity item) {
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();

        this.price = BigDecimal.valueOf(item.getProduct().getPrice())
                .setScale(2, RoundingMode.HALF_UP);

        this.quantity = item.getQuantity();

        this.itemTotal = this.price
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }
}