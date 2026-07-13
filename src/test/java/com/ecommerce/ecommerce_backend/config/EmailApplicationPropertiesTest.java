package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;

@SpringBootTest(properties = {
        "email.from=test-email@ecommerce-test.com",
        "app.frontend.url=http://localhost:4200",
        "app.email.verification.enabled=true"
})
public class EmailApplicationPropertiesTest {

    @Autowired
    private EmailProperties emailProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Test
    public void emailAndApplicationPropertiesAreBoundCorrectly() {

        Assertions.assertEquals(
                "test-email@ecommerce-test.com",
                emailProperties.from()
        );

        Assertions.assertEquals(
                URI.create("http://localhost:4200"),
                applicationProperties.frontend().url()
        );

        Assertions.assertTrue(
                applicationProperties.email()
                        .verification()
                        .enabled()
        );
    }
}