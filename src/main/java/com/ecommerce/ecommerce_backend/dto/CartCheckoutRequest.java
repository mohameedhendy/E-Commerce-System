package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartCheckoutRequest {

    @NotNull(message = "Address id is required")
    private Long addressId;
}