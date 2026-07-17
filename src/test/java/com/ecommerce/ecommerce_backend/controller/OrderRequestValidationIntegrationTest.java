package com.ecommerce.ecommerce_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderRequestValidationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithUserDetails("UserA")
    public void duplicateProductIdsReturnBadRequest()
            throws Exception {

        mvc.perform(
                        post("/order")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                                {
                                                  "addressId": 1,
                                                  "items": [
                                                    {
                                                      "productId": 1,
                                                      "quantity": 2
                                                    },
                                                    {
                                                      "productId": 1,
                                                      "quantity": 3
                                                    }
                                                  ]
                                                }
                                                """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Validation failed"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.items"
                        )
                                .value(
                                        "Order items must contain unique product ids"
                                )
                );
    }
}