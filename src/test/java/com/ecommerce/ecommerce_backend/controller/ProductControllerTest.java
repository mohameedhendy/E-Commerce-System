package com.ecommerce.ecommerce_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testProductListIsPublic() throws Exception {
        mvc.perform(get("/product"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthenticatedUserCannotCreateReview() throws Exception {
        mvc.perform(
                        post("/product/1/review")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "rating": 5,
                                          "comment": "Great product"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void negativeReviewPageReturnsBadRequest()
            throws Exception {

        mvc.perform(
                        get("/product/1/reviews")
                                .param("page", "-1")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void zeroReviewPageSizeReturnsBadRequest()
            throws Exception {

        mvc.perform(
                        get("/product/1/reviews")
                                .param("page", "0")
                                .param("size", "0")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void excessiveReviewPageSizeReturnsBadRequest()
            throws Exception {

        mvc.perform(
                        get("/product/1/reviews")
                                .param("page", "0")
                                .param("size", "51")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("UserA")
    public void reviewCommentLongerThanLimitReturnsBadRequest()
            throws Exception {

        String longComment = "a".repeat(256);

        mvc.perform(
                        post("/product/1/review")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "rating": 5,
                                          "comment": "%s"
                                        }
                                        """.formatted(longComment))
                )
                .andExpect(status().isBadRequest());
    }
}