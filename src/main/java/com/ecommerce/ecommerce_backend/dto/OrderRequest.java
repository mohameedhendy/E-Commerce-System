package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "Address id is required")
    private Long addressId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

}