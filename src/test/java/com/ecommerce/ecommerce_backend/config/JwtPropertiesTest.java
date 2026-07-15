package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.algorithm.key=JwtPropertiesTestSecretKey12345678901234567890",
        "jwt.issuer=eCommerce-test",
        "jwt.expiry-in-seconds=604800",
        "jwt.refresh-expiry-in-seconds=2592000"
})
public class JwtPropertiesTest {

    @Autowired
    private JwtProperties jwtProperties;

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
                604800L,
                jwtProperties.expiryInSeconds()
        );

        Assertions.assertEquals(
                2592000L,
                jwtProperties.refreshExpiryInSeconds()
        );
    }
}