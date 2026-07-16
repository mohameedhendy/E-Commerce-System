package com.ecommerce.ecommerce_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "app.security.auth-rate-limit.enabled=true",
                "app.security.auth-rate-limit.max-requests=2",
                "app.security.auth-rate-limit.window-seconds=60"
        }
)
@AutoConfigureMockMvc
public class AuthRateLimitIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void loginReturnsTooManyRequestsAfterLimit()
            throws Exception {

        RequestPostProcessor clientAddress =
                request -> {

                    request.setRemoteAddr(
                            "203.0.113.10"
                    );

                    return request;
                };

        String requestBody = """
                {
                  "username": "UserA",
                  "password": "WrongPassword123"
                }
                """;

        mvc.perform(
                        post("/auth/login")
                                .with(clientAddress)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isUnauthorized()
                );

        mvc.perform(
                        post("/auth/login")
                                .with(clientAddress)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isUnauthorized()
                );

        mvc.perform(
                        post("/auth/login")
                                .with(clientAddress)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isTooManyRequests()
                )
                .andExpect(
                        header().exists(
                                "Retry-After"
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(429)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Too Many Requests"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Too many requests. Please try again later."
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.validationErrors")
                                .isEmpty()
                );
    }
}