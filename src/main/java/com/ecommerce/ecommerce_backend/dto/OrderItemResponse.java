package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.ProductOrderQuantity;

public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;

    public OrderItemResponse(ProductOrderQuantity item) {
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();
        this.price = item.getProduct().getPrice();
        this.quantity = item.getQuantity();
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }
}