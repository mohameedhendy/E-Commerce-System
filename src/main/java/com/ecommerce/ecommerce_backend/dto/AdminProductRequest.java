package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(
            max = 255,
            message = "Product name must not exceed 255 characters"
    )
    private String name;

    @NotBlank(message = "Short description is required")
    @Size(
            max = 255,
            message = "Short description must not exceed 255 characters"
    )
    private String shortDescription;

    @NotBlank(message = "Long description is required")
    @Size(
            max = 255,
            message = "Long description must not exceed 255 characters"
    )
    private String longDescription;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be at least 0.01"
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Price must have at most 2 decimal places"
    )
    private BigDecimal price;
}
