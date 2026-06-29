package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Order;

import java.util.List;

public class OrderResponse {

    private Long id;
    private AddressResponse address;
    private List<OrderItemResponse> items;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.address = new AddressResponse(order.getAddress());
        this.items = order.getQuantities()
                .stream()
                .map(OrderItemResponse::new)
                .toList();
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
}