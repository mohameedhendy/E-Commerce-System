package com.ecommerce.ecommerce_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EncryptionServiceTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    public void testPasswordEncryptionAndVerification() {
        String password = "Password123";

        String encryptedPassword =
                encryptionService.encryptPassword(password);

        assertNotEquals(
                password,
                encryptedPassword,
                "Password should not be stored as plain text."
        );

        assertTrue(
                encryptionService.verifyPassword(
                        password,
                        encryptedPassword
                ),
                "Correct password should match its encrypted value."
        );

        assertFalse(
                encryptionService.verifyPassword(
                        "WrongPassword123",
                        encryptedPassword
                ),
                "Incorrect password should not match."
        );
    }

    @Test
    public void testSamePasswordProducesDifferentHashes() {
        String password = "Password123";

        String firstHash =
                encryptionService.encryptPassword(password);

        String secondHash =
                encryptionService.encryptPassword(password);

        assertNotEquals(
                firstHash,
                secondHash,
                "Each password encryption should use a unique salt."
        );

        assertTrue(
                encryptionService.verifyPassword(password, firstHash)
        );

        assertTrue(
                encryptionService.verifyPassword(password, secondHash)
        );
    }
}