package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotNull
    @NotBlank
    @Size(max = 512)
    private String addressLine1;

    @Size(max = 512)
    private String addressLine2;

    @NotNull
    @NotBlank
    @Size(max = 75)
    private String country;

    @NotNull
    @NotBlank
    private String city;

}