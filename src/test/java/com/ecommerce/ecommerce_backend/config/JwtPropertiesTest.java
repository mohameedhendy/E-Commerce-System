package com.ecommerce.ecommerce_backend.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest(properties = {
        "jwt.algorithm.key=JwtPropertiesTestSecretKey12345678901234567890",
        "jwt.issuer=eCommerce-test",
        "jwt.audience=ecommerce-api",
        "jwt.expiry-in-seconds=604800",
        "jwt.refresh-expiry-in-seconds=2592000",
        "jwt.verification-expiry-in-seconds=86400",
        "jwt.password-reset-expiry-in-seconds=1800"
})
public class JwtPropertiesTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private Validator validator;

    @Test
    public void jwtPropertiesAreBoundCorrectly() {

        Assertions.assertNotNull(
                jwtProperties.algorithm()
        );

        Assertions.assertFalse(
                jwtProperties
                        .algorithm()
                        .key()
                        .isBlank()
        );

        Assertions.assertEquals(
                "eCommerce-test",
                jwtProperties.issuer()
        );

        Assertions.assertEquals(
                "ecommerce-api",
                jwtProperties.audience()
        );

        Assertions.assertEquals(
                604800L,
                jwtProperties.expiryInSeconds()
        );

        Assertions.assertEquals(
                2592000L,
                jwtProperties.refreshExpiryInSeconds()
        );

        Assertions.assertEquals(
                86400L,
                jwtProperties.verificationExpiryInSeconds()
        );

        Assertions.assertEquals(
                1800L,
                jwtProperties.passwordResetExpiryInSeconds()
        );
    }

    @Test
    public void configuredJwtPropertiesAreValid() {

        Set<ConstraintViolation<JwtProperties>> violations =
                validator.validate(
                        jwtProperties
                );

        Assertions.assertTrue(
                violations.isEmpty(),
                "Configured JWT properties must be valid."
        );
    }

    @Test
    public void shortJwtSecretIsRejected() {

        JwtProperties invalidProperties =
                new JwtProperties(
                        new JwtProperties.AlgorithmProperties(
                                "short-secret"
                        ),
                        "eCommerce",
                        "ecommerce-api",
                        3600L,
                        2592000L,
                        86400L,
                        1800L
                );

        Set<ConstraintViolation<JwtProperties>> violations =
                validator.validate(
                        invalidProperties
                );

        Assertions.assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals("algorithm.key")
                        ),
                "JWT secrets shorter than 32 characters must be rejected."
        );
    }
}