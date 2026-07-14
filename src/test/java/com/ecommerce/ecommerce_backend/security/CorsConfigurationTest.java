package com.ecommerce.ecommerce_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CorsConfigurationTest {

    private static final String ALLOWED_ORIGIN =
            "http://localhost:3000";

    private static final String DISALLOWED_ORIGIN =
            "http://untrusted.example";

    @Autowired
    private MockMvc mvc;

    @Test
    public void preflightFromConfiguredFrontendIsAllowed()
            throws Exception {

        mvc.perform(
                        options("/auth/login")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        ALLOWED_ORIGIN
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "POST"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "Authorization, Content-Type"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                ALLOWED_ORIGIN
                        )
                )
                .andExpect(
                        header().exists(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS
                        )
                )
                .andExpect(
                        header().exists(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS
                        )
                );
    }

    @Test
    public void preflightFromUntrustedOriginIsRejected()
            throws Exception {

        mvc.perform(
                        options("/auth/login")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        DISALLOWED_ORIGIN
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "POST"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        header().doesNotExist(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                        )
                );
    }
}