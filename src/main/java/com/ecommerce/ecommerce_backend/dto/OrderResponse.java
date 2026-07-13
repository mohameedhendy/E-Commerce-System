package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Order;
import lombok.Getter;
import lombok.Setter;
import com.ecommerce.ecommerce_backend.model.ShippingAddress;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;


@Getter
public class OrderResponse {

    private Long id;
    private AddressResponse address;
    private List<OrderItemResponse> items;
    private BigDecimal orderTotal;
    private String status;
    private LocalDateTime createdAt;

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

        this.orderTotal = order.getTotalAmount()
                .setScale(2, RoundingMode.HALF_UP);

        this.status = order.getStatus() != null
                ? order.getStatus().name()
                : null;

        this.createdAt = order.getCreatedAt();
    }
}