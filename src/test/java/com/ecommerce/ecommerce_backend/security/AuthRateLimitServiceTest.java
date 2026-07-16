package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.config.AuthRateLimitProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthRateLimitServiceTest {

    @Test
    public void requestsBeyondConfiguredLimitAreRejected() {

        AuthRateLimitProperties properties =
                createProperties();

        AuthRateLimitService service =
                new AuthRateLimitService(
                        properties
                );

        AuthRateLimitService.RateLimitDecision
                firstDecision =
                service.tryAcquire(
                        "127.0.0.1|POST|/auth/login"
                );

        AuthRateLimitService.RateLimitDecision
                secondDecision =
                service.tryAcquire(
                        "127.0.0.1|POST|/auth/login"
                );

        AuthRateLimitService.RateLimitDecision
                thirdDecision =
                service.tryAcquire(
                        "127.0.0.1|POST|/auth/login"
                );

        Assertions.assertTrue(
                firstDecision.allowed()
        );

        Assertions.assertTrue(
                secondDecision.allowed()
        );

        Assertions.assertFalse(
                thirdDecision.allowed()
        );

        Assertions.assertTrue(
                thirdDecision.retryAfterSeconds() > 0
        );
    }

    @Test
    public void differentEndpointsHaveIndependentLimits() {

        AuthRateLimitProperties properties =
                createProperties();

        AuthRateLimitService service =
                new AuthRateLimitService(
                        properties
                );

        service.tryAcquire(
                "127.0.0.1|POST|/auth/login"
        );

        service.tryAcquire(
                "127.0.0.1|POST|/auth/login"
        );

        Assertions.assertFalse(
                service.tryAcquire(
                        "127.0.0.1|POST|/auth/login"
                ).allowed()
        );

        Assertions.assertTrue(
                service.tryAcquire(
                        "127.0.0.1|POST|/auth/forgot"
                ).allowed()
        );
    }

    private AuthRateLimitProperties
    createProperties() {

        AuthRateLimitProperties properties =
                new AuthRateLimitProperties();

        properties.setEnabled(true);
        properties.setMaxRequests(2);
        properties.setWindowSeconds(60L);

        return properties;
    }
}