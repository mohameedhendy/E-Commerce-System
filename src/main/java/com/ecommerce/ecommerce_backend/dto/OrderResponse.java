package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderResponse {

    private Long id;
    private AddressResponse address;
    private List<OrderItemResponse> items;
    private BigDecimal orderTotal;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.address = new AddressResponse(order.getAddress());
        this.items = order.getQuantities()
                .stream()
                .map(OrderItemResponse::new)
                .toList();

        this.orderTotal = this.items.stream()
                .map(OrderItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public AddressResponse getAddress() {
        return address;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }
}