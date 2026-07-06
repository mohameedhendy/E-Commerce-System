package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOrderStatusRequest {

    @NotBlank(message = "Order status is required")
    @Pattern(
            regexp = "CONFIRMED|CANCELLED",
            message = "Order status must be one of: CONFIRMED, CANCELLED"
    )
    private String status;
}