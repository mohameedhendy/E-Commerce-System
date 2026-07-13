package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AdminProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Short description is required")
    private String shortDescription;

    @NotBlank(message = "Long description is required")
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

    @NotNull(message = "Stock quantity is required")
    @Min(
            value = 0,
            message = "Stock quantity must be 0 or greater"
    )
    private Integer stockQuantity;

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }
}