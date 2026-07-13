package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "encryption.salt")
public record EncryptionProperties(

        @NotNull
        @Min(
                value = 4,
                message = "BCrypt rounds must be at least 4"
        )
        @Max(
                value = 31,
                message = "BCrypt rounds must not exceed 31"
        )
        Integer rounds
) {
}