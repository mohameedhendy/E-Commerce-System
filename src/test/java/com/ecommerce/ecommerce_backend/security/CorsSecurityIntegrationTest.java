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
public class CorsSecurityIntegrationTest {

    private static final String TRUSTED_ORIGIN =
            "http://localhost:3000";

    @Autowired
    private MockMvc mvc;

    @Test
    public void trustedFrontendOriginIsAllowed()
            throws Exception {

        mvc.perform(
                        options("/auth/login")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        TRUSTED_ORIGIN
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_METHOD,
                                        "POST"
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_HEADERS,
                                        HttpHeaders.CONTENT_TYPE
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_ORIGIN,
                                TRUSTED_ORIGIN
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_METHODS,
                                org.hamcrest.Matchers
                                        .containsString("POST")
                        )
                );
    }

    @Test
    public void untrustedOriginIsRejected()
            throws Exception {

        mvc.perform(
                        options("/auth/login")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "https://malicious.example"
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_METHOD,
                                        "POST"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }
}