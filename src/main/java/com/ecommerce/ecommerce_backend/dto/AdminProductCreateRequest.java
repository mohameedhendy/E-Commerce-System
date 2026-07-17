package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProductCreateRequest
        extends AdminProductRequest {

    @NotNull(message = "Stock quantity is required")
    @Min(
            value = 0,
            message = "Stock quantity must be 0 or greater"
    )
    private Integer stockQuantity;
}
