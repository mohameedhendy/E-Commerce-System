package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.ShippingAddress;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderResponse {

    private final Long id;
    private final AddressResponse address;
    private final List<OrderItemResponse> items;
    private final BigDecimal orderTotal;
    private final String status;
    private final LocalDateTime createdAt;

    public OrderResponse(Order order) {

        this.id = order.getId();

        ShippingAddress shippingAddress =
                order.getShippingAddress();

        this.address = new AddressResponse(
                order.getAddress().getId(),
                shippingAddress.getAddressLine1(),
                shippingAddress.getAddressLine2(),
                shippingAddress.getCountry(),
                shippingAddress.getCity()
        );

        this.items = order.getQuantities()
                .stream()
                .map(OrderItemResponse::new)
                .toList();

        this.orderTotal = MoneyUtils.scale(
                order.getTotalAmount()
        );

        this.status = order.getStatus() == null
                ? null
                : order.getStatus().name();

        this.createdAt = order.getCreatedAt();
    }
}