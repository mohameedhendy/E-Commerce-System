package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataSerializationTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void localUserDoesNotSerializeSensitiveFields()
            throws Exception {

        LocalUser user = new LocalUser();

        user.setId(1L);
        user.setUsername("UserA");
        user.setEmail("user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("encoded-password");
        user.setPasswordResetVersion(7L);
        user.setRefreshTokenVersion(9L);

        JsonNode json =
                objectMapper.readTree(
                        objectMapper.writeValueAsString(user)
                );

        assertThat(json.has("password"))
                .isFalse();

        assertThat(json.has("passwordResetVersion"))
                .isFalse();

        assertThat(json.has("refreshTokenVersion"))
                .isFalse();

        assertThat(json.path("username").asText())
                .isEqualTo("UserA");
    }
}