package com.ecommerce.ecommerce_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails("UserA")
    public void testUserAAuthenticatedOrderList() throws Exception {
        testAuthenticatedOrderList(Set.of(1L, 2L, 3L));
    }

    @Test
    @WithUserDetails("UserB")
    public void testUserBAuthenticatedOrderList() throws Exception {
        testAuthenticatedOrderList(Set.of());
    }

    private void testAuthenticatedOrderList(Set<Long> expectedOrderIds)
            throws Exception {

        mvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();

                    JsonNode response = objectMapper.readTree(json);
                    JsonNode content = response.get("content");

                    Assertions.assertNotNull(
                            content,
                            "Paginated response should contain a content field."
                    );

                    Assertions.assertTrue(
                            content.isArray(),
                            "The content field should be an array."
                    );

                    Set<Long> actualOrderIds = new HashSet<>();

                    content.forEach(order ->
                            actualOrderIds.add(order.get("id").asLong())
                    );

                    Assertions.assertEquals(
                            expectedOrderIds,
                            actualOrderIds,
                            "The user should only receive their own orders."
                    );
                });
    }

    @Test
    public void testUnauthenticatedOrderList() throws Exception {
        mvc.perform(get("/order"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }
}