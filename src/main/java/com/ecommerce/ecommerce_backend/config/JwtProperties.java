package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

        @NotBlank
        String audience,

        @NotNull
        @Positive
        Long expiryInSeconds,

        @NotNull
        @Positive
        Long refreshExpiryInSeconds,

        @NotNull
        @Positive
        Long verificationExpiryInSeconds,

        @NotNull
        @Positive
        Long passwordResetExpiryInSeconds
) {

    public record AlgorithmProperties(

            @NotBlank
            @Size(
                    min = 32,
                    message = "JWT secret must contain at least 32 characters"
            )
            String key
    ) {
    }
}