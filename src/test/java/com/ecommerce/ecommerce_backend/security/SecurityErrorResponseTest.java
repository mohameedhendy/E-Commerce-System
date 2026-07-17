package com.ecommerce.ecommerce_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityErrorResponseTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void unauthenticatedRequestReturnsStructuredResponse()
            throws Exception {

        mvc.perform(
                        get("/cart")
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status").value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Authentication is required to access this resource"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );
    }

    @Test
    public void invalidBearerTokenReturnsStructuredResponse()
            throws Exception {

        mvc.perform(
                        get("/cart")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer InvalidToken"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status").value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Authentication is required to access this resource"
                                )
                );
    }

    @Test
    @WithMockUser(roles = "USER")
    public void nonAdminUserReturnsStructuredForbiddenResponse()
            throws Exception {

        mvc.perform(
                        get("/admin/security-check")
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status").value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Forbidden")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "You do not have permission to access this resource"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );
    }
}