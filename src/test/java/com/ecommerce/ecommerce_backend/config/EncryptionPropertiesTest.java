package com.ecommerce.ecommerce_backend.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(properties = {
        "encryption.salt.rounds=4"
})
public class EncryptionPropertiesTest {

    @Autowired
    private EncryptionProperties encryptionProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void encryptionPropertiesAreBoundCorrectly() {

        Assertions.assertEquals(
                4,
                encryptionProperties.rounds()
        );
    }

    @Test
    public void passwordEncoderBeanUsesValidConfiguration() {

        String password = "StrongPassword123";

        String encodedPassword =
                passwordEncoder.encode(password);

        Assertions.assertNotEquals(
                password,
                encodedPassword
        );

        Assertions.assertTrue(
                passwordEncoder.matches(
                        password,
                        encodedPassword
                )
        );
    }
}