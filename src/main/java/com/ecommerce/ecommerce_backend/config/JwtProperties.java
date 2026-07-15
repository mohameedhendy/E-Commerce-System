package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        @Valid
        @NotNull
        AlgorithmProperties algorithm,

        @NotBlank
        String issuer,

        @NotNull
        @Positive
        Long expiryInSeconds,

        @NotNull
        @Positive
        Long refreshExpiryInSeconds
) {

    public record AlgorithmProperties(

            @NotBlank
            String key
    ) {
    }
}