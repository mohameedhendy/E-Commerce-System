package com.ecommerce.ecommerce_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CartControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void unauthenticatedUserCannotAccessCart()
            throws Exception {

        mvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("UserA")
    public void authenticatedUserReceivesEmptyCart()
            throws Exception {

        mvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items").isEmpty()
                )
                .andExpect(
                        jsonPath("$.totalItems").value(0)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(0)
                )
                .andExpect(
                        jsonPath("$.subtotal").value(0.00)
                );
    }

    @Test
    @WithUserDetails("UserA")
    public void authenticatedUserCanManageCartItems()
            throws Exception {

        String addResponse = mvc.perform(
                        post("/cart/items")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "productId": 1,
                                          "quantity": 2
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalItems").value(1)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(2)
                )
                .andExpect(
                        jsonPath("$.items[0].productId").value(1)
                )
                .andExpect(
                        jsonPath("$.items[0].quantity").value(2)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode addedCart =
                objectMapper.readTree(addResponse);

        Long itemId = addedCart
                .get("items")
                .get(0)
                .get("id")
                .asLong();

        Assertions.assertTrue(
                itemId > 0,
                "The saved cart item should have an ID."
        );

        mvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalItems").value(1)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(2)
                )
                .andExpect(
                        jsonPath("$.items[0].id")
                                .value(itemId)
                );

        mvc.perform(
                        put("/cart/items/{itemId}", itemId)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "quantity": 4
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalItems").value(1)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(4)
                )
                .andExpect(
                        jsonPath("$.items[0].quantity").value(4)
                );

        mvc.perform(
                        delete(
                                "/cart/items/{itemId}",
                                itemId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items").isEmpty()
                )
                .andExpect(
                        jsonPath("$.totalItems").value(0)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(0)
                );
    }

    @Test
    @WithUserDetails("UserA")
    public void authenticatedUserCanClearCart()
            throws Exception {

        addProductToCart(1L, 1);
        addProductToCart(2L, 2);

        mvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalItems").value(2)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(3)
                );

        mvc.perform(delete("/cart"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items").isEmpty()
                )
                .andExpect(
                        jsonPath("$.totalItems").value(0)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(0)
                )
                .andExpect(
                        jsonPath("$.subtotal").value(0.00)
                );
    }

    @Test
    @WithUserDetails("UserA")
    public void invalidCartQuantityReturnsBadRequest()
            throws Exception {

        mvc.perform(
                        post("/cart/items")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "productId": 1,
                                          "quantity": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400)
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.quantity"
                        ).value(
                                "Quantity must be at least 1"
                        )
                );
    }

    @Test
    @WithUserDetails("UserA")
    public void missingCartItemReturnsNotFound()
            throws Exception {

        mvc.perform(
                        put(
                                "/cart/items/{itemId}",
                                Long.MAX_VALUE
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "quantity": 1
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status").value(404)
                )
                .andExpect(
                        jsonPath("$.message").value(
                                "Cart item was not found"
                        )
                );
    }

    private void addProductToCart(
            Long productId,
            int quantity
    ) throws Exception {

        mvc.perform(
                        post("/cart/items")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "productId": %d,
                                          "quantity": %d
                                        }
                                        """.formatted(
                                        productId,
                                        quantity
                                ))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("UserA")
    public void authenticatedUserCanCheckoutCart()
            throws Exception {

        addProductToCart(1L, 2);

        mvc.perform(
                        post("/cart/checkout")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "addressId": 1
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id").isNumber()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.items[0].productId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.items[0].quantity")
                                .value(2)
                );

        mvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items").isEmpty()
                )
                .andExpect(
                        jsonPath("$.totalItems").value(0)
                )
                .andExpect(
                        jsonPath("$.totalQuantity").value(0)
                );
    }
}