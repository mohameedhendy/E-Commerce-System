package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(
        prefix = "app.security.auth-rate-limit"
)
@Validated
@Getter
@Setter
public class AuthRateLimitProperties {

    private boolean enabled = true;

    @Min(1)
    private int maxRequests = 10;

    @Min(1)
    private long windowSeconds = 60;
}