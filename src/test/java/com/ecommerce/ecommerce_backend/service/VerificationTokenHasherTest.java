package com.ecommerce.ecommerce_backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VerificationTokenHasherTest {

    private final VerificationTokenHasher hasher =
            new VerificationTokenHasher();

    @Test
    void hashIsDeterministicAndDoesNotExposeRawToken() {

        String rawToken = "verification-token-value";

        String firstHash = hasher.hash(rawToken);
        String secondHash = hasher.hash(rawToken);

        assertThat(firstHash)
                .isEqualTo(secondHash)
                .isNotEqualTo(rawToken)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void differentTokensProduceDifferentHashes() {

        assertThat(
                hasher.hash("first-token")
        ).isNotEqualTo(
                hasher.hash("second-token")
        );
    }

    @Test
    void blankTokenIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> hasher.hash(" ")
        );
    }
}