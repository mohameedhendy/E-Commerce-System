package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotBlank(message = "Address line 1 is required")
    @Size(
            max = 512,
            message = "Address line 1 must not exceed 512 characters"
    )
    private String addressLine1;

    @Size(
            max = 512,
            message = "Address line 2 must not exceed 512 characters"
    )
    private String addressLine2;

    @NotBlank(message = "Country is required")
    @Size(
            max = 75,
            message = "Country must not exceed 75 characters"
    )
    private String country;

    @NotBlank(message = "City is required")
    @Size(
            max = 255,
            message = "City must not exceed 255 characters"
    )
    private String city;
}