package com.ecommerce.ecommerce_backend.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class VerificationTokenHasher {

    private static final String HASH_ALGORITHM =
            "SHA-256";

    public String hash(
            String token
    ) {

        if (token == null
                || token.isBlank()) {

            throw new IllegalArgumentException(
                    "Verification token must not be blank"
            );
        }

        try {

            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            HASH_ALGORITHM
                    );

            byte[] hash =
                    messageDigest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    ex
            );
        }
    }
}