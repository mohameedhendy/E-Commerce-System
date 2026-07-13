package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(

        @Valid
        @NotNull
        EmailSettings email,

        @Valid
        @NotNull
        FrontendSettings frontend
) {

    public record EmailSettings(

            @Valid
            @NotNull
            VerificationSettings verification
    ) {
    }

    public record VerificationSettings(
            boolean enabled
    ) {
    }

    public record FrontendSettings(

            @NotNull
            URI url
    ) {
    }
}