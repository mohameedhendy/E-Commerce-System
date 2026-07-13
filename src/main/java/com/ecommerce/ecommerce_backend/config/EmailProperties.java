package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "email")
public record EmailProperties(

        @NotBlank
        @Email
        String from
) {
}