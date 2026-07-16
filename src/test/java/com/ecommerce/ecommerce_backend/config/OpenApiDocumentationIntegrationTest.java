package com.ecommerce.ecommerce_backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void openApiDocumentationIsPublicAndContainsJwtScheme()
            throws Exception {

        mvc.perform(
                        get("/v3/api-docs")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        header().string(
                                "Content-Security-Policy",
                                "default-src 'none'; "
                                        + "frame-ancestors 'none'"
                        )
                );

        JsonNode documentation = getOpenApiDocumentation();

        assertTrue(
                documentation.path("info")
                        .path("title")
                        .asText()
                        .equals("E-Commerce Backend API")
        );

        JsonNode bearerScheme = documentation
                .path("components")
                .path("securitySchemes")
                .path(OpenApiConfig.BEARER_AUTH_SCHEME);

        assertTrue(
                bearerScheme.path("type")
                        .asText()
                        .equals("http")
        );

        assertTrue(
                bearerScheme.path("scheme")
                        .asText()
                        .equals("bearer")
        );

        assertTrue(
                bearerScheme.path("bearerFormat")
                        .asText()
                        .equals("JWT")
        );

        mvc.perform(
                        get("/swagger-ui.html")
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/swagger-ui/index.html"
                        )
                );

        mvc.perform(
                        get("/swagger-ui/index.html")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        header().string(
                                "Content-Security-Policy",
                                containsString(
                                        "script-src 'self'"
                                )
                        )
                );

        mvc.perform(
                        get("/swagger-ui/swagger-ui-bundle.js")
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    public void protectedAndPublicOperationsHaveExpectedSecurityDocumentation()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertBearerAuth(paths, "/cart", "get");
        assertBearerAuth(paths, "/order", "get");
        assertBearerAuth(
                paths,
                "/user/{userId}/address",
                "get"
        );
        assertBearerAuth(
                paths,
                "/reviews/{reviewId}",
                "put"
        );

        assertBearerAuth(
                paths,
                "/admin/dashboard/summary",
                "get"
        );
        assertBearerAuth(
                paths,
                "/admin/order",
                "get"
        );
        assertBearerAuth(
                paths,
                "/admin/product",
                "get"
        );

        assertBearerAuth(
                paths,
                "/auth/logout-all",
                "post"
        );
        assertBearerAuth(
                paths,
                "/auth/sessions",
                "get"
        );
        assertBearerAuth(
                paths,
                "/auth/sessions/{sessionId}",
                "delete"
        );
        assertBearerAuth(
                paths,
                "/auth/me",
                "get"
        );

        assertBearerAuth(
                paths,
                "/product/{productId}/review",
                "post"
        );

        assertPublicOperation(
                paths,
                "/auth/login",
                "post"
        );
        assertPublicOperation(
                paths,
                "/auth/register",
                "post"
        );
        assertPublicOperation(
                paths,
                "/auth/refresh",
                "post"
        );
        assertPublicOperation(
                paths,
                "/auth/logout",
                "post"
        );
        assertPublicOperation(
                paths,
                "/auth/forgot",
                "post"
        );
        assertPublicOperation(
                paths,
                "/auth/reset",
                "post"
        );
        assertPublicOperation(
                paths,
                "/auth/verify",
                "post"
        );

        assertPublicOperation(
                paths,
                "/product",
                "get"
        );
        assertPublicOperation(
                paths,
                "/product/{productId}",
                "get"
        );
        assertPublicOperation(
                paths,
                "/product/{productId}/reviews",
                "get"
        );
    }


    @Test
    public void operationsUseBusinessFriendlyTags()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertOperationTag(
                paths,
                "/auth/login",
                "post",
                "Authentication"
        );

        assertOperationTag(
                paths,
                "/product",
                "get",
                "Products"
        );

        assertOperationTag(
                paths,
                "/cart",
                "get",
                "Shopping Cart"
        );

        assertOperationTag(
                paths,
                "/order",
                "get",
                "Orders"
        );

        assertOperationTag(
                paths,
                "/reviews/{reviewId}",
                "put",
                "Reviews"
        );

        assertOperationTag(
                paths,
                "/user/{userId}/address",
                "get",
                "User Addresses"
        );

        assertOperationTag(
                paths,
                "/admin/dashboard/summary",
                "get",
                "Admin Dashboard"
        );

        assertOperationTag(
                paths,
                "/admin/order",
                "get",
                "Admin Orders"
        );

        assertOperationTag(
                paths,
                "/admin/product",
                "get",
                "Admin Products"
        );
    }


    @Test
    public void authenticationOperationsExposeClearSummaries()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertOperationSummary(
                paths,
                "/auth/register",
                "post",
                "Register a new customer account"
        );

        assertOperationSummary(
                paths,
                "/auth/login",
                "post",
                "Authenticate a user"
        );

        assertOperationSummary(
                paths,
                "/auth/refresh",
                "post",
                "Issue new tokens using a refresh token"
        );

        assertOperationSummary(
                paths,
                "/auth/logout",
                "post",
                "Revoke a refresh token"
        );

        assertOperationSummary(
                paths,
                "/auth/logout-all",
                "post",
                "Revoke all active sessions"
        );

        assertOperationSummary(
                paths,
                "/auth/sessions",
                "get",
                "List active authentication sessions"
        );

        assertOperationSummary(
                paths,
                "/auth/sessions/{sessionId}",
                "delete",
                "Revoke a specific authentication session"
        );

        assertOperationSummary(
                paths,
                "/auth/verify",
                "post",
                "Verify a customer email address"
        );

        assertOperationSummary(
                paths,
                "/auth/me",
                "get",
                "Get the authenticated customer profile"
        );

        assertOperationSummary(
                paths,
                "/auth/forgot",
                "post",
                "Request a password reset"
        );

        assertOperationSummary(
                paths,
                "/auth/reset",
                "post",
                "Reset a customer password"
        );
    }


    @Test
    public void businessOperationsExposeClearSummaries()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertOperationSummary(
                paths,
                "/product",
                "get",
                "List products"
        );

        assertOperationSummary(
                paths,
                "/cart",
                "get",
                "Get the authenticated customer's cart"
        );

        assertOperationSummary(
                paths,
                "/order",
                "get",
                "List the authenticated customer's orders"
        );

        assertOperationSummary(
                paths,
                "/reviews/{reviewId}",
                "put",
                "Update an authenticated customer's review"
        );

        assertOperationSummary(
                paths,
                "/user/{userId}/address",
                "get",
                "List a customer's addresses"
        );

        assertOperationSummary(
                paths,
                "/admin/dashboard/summary",
                "get",
                "Get administrative dashboard summary"
        );

        assertOperationSummary(
                paths,
                "/admin/order",
                "get",
                "List all orders for administration"
        );

        assertOperationSummary(
                paths,
                "/admin/product",
                "get",
                "List products for administration"
        );
    }


    @Test
    public void operationsUseJsonRequestAndResponseMediaTypes()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertJsonRequestBody(
                paths,
                "/auth/login",
                "post"
        );

        assertJsonResponse(
                paths,
                "/auth/login",
                "post",
                "200"
        );

        assertJsonResponse(
                paths,
                "/product",
                "get",
                "200"
        );

        assertJsonResponse(
                paths,
                "/cart",
                "get",
                "200"
        );
    }


    @Test
    public void reusableErrorResponsesUseApiErrorResponseSchema()
            throws Exception {

        JsonNode documentation =
                getOpenApiDocumentation();

        JsonNode errorSchema = documentation
                .path("components")
                .path("schemas")
                .path("ApiErrorResponse");

        assertFalse(
                errorSchema.isMissingNode(),
                "ApiErrorResponse schema is missing"
        );

        JsonNode properties =
                errorSchema.path("properties");

        assertTrue(properties.has("timestamp"));
        assertTrue(properties.has("status"));
        assertTrue(properties.has("error"));
        assertTrue(properties.has("message"));
        assertTrue(properties.has("validationErrors"));

        assertReusableErrorResponse(
                documentation,
                OpenApiConfig.BAD_REQUEST_RESPONSE
        );

        assertReusableErrorResponse(
                documentation,
                OpenApiConfig.UNAUTHORIZED_RESPONSE
        );

        assertReusableErrorResponse(
                documentation,
                OpenApiConfig.FORBIDDEN_RESPONSE
        );

        assertReusableErrorResponse(
                documentation,
                OpenApiConfig.NOT_FOUND_RESPONSE
        );

        assertReusableErrorResponse(
                documentation,
                OpenApiConfig.CONFLICT_RESPONSE
        );

        assertReusableErrorResponse(
                documentation,
                OpenApiConfig.INTERNAL_SERVER_ERROR_RESPONSE
        );
    }


    @Test
    public void commonErrorResponsesAreLinkedToOperations()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertResponseReference(
                paths,
                "/auth/login",
                "post",
                "400",
                OpenApiConfig.BAD_REQUEST_RESPONSE
        );

        assertResponseReference(
                paths,
                "/auth/login",
                "post",
                "500",
                OpenApiConfig.INTERNAL_SERVER_ERROR_RESPONSE
        );

        assertResponseMissing(
                paths,
                "/auth/login",
                "post",
                "401"
        );

        assertResponseMissing(
                paths,
                "/auth/login",
                "post",
                "403"
        );

        assertResponseReference(
                paths,
                "/cart",
                "get",
                "401",
                OpenApiConfig.UNAUTHORIZED_RESPONSE
        );

        assertResponseReference(
                paths,
                "/cart",
                "get",
                "403",
                OpenApiConfig.FORBIDDEN_RESPONSE
        );

        assertResponseReference(
                paths,
                "/cart",
                "get",
                "500",
                OpenApiConfig.INTERNAL_SERVER_ERROR_RESPONSE
        );

        assertResponseMissing(
                paths,
                "/cart",
                "get",
                "400"
        );

        assertResponseReference(
                paths,
                "/product",
                "get",
                "400",
                OpenApiConfig.BAD_REQUEST_RESPONSE
        );

        assertResponseReference(
                paths,
                "/product",
                "get",
                "500",
                OpenApiConfig.INTERNAL_SERVER_ERROR_RESPONSE
        );

        assertResponseMissing(
                paths,
                "/product",
                "get",
                "401"
        );
    }


    @Test
    public void resourceSpecificErrorResponsesAreLinkedToRelevantOperations()
            throws Exception {

        JsonNode paths = getOpenApiDocumentation()
                .path("paths");

        assertResponseReference(
                paths,
                "/product/{productId}",
                "get",
                "404",
                OpenApiConfig.NOT_FOUND_RESPONSE
        );

        assertResponseMissing(
                paths,
                "/product/{productId}",
                "get",
                "409"
        );

        assertResponseReference(
                paths,
                "/auth/register",
                "post",
                "409",
                OpenApiConfig.CONFLICT_RESPONSE
        );

        assertResponseMissing(
                paths,
                "/auth/register",
                "post",
                "404"
        );

        assertResponseReference(
                paths,
                "/order",
                "post",
                "404",
                OpenApiConfig.NOT_FOUND_RESPONSE
        );

        assertResponseReference(
                paths,
                "/order",
                "post",
                "409",
                OpenApiConfig.CONFLICT_RESPONSE
        );

        assertResponseReference(
                paths,
                "/admin/product/{productId}",
                "put",
                "404",
                OpenApiConfig.NOT_FOUND_RESPONSE
        );

        assertResponseReference(
                paths,
                "/admin/product/{productId}",
                "put",
                "409",
                OpenApiConfig.CONFLICT_RESPONSE
        );

        assertResponseMissing(
                paths,
                "/product",
                "get",
                "404"
        );

        assertResponseMissing(
                paths,
                "/product",
                "get",
                "409"
        );
    }

    private JsonNode getOpenApiDocumentation()
            throws Exception {

        MvcResult result = mvc.perform(
                        get("/v3/api-docs")
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn();

        return objectMapper.readTree(
                result.getResponse()
                        .getContentAsString()
        );
    }






    private void assertResponseReference(
            JsonNode paths,
            String path,
            String method,
            String statusCode,
            String responseName
    ) {

        JsonNode response = paths
                .path(path)
                .path(method)
                .path("responses")
                .path(statusCode);

        assertEquals(
                "#/components/responses/" + responseName,
                response.path("$ref").asText(),
                () -> "Unexpected response reference: "
                        + method.toUpperCase()
                        + " "
                        + path
                        + " status "
                        + statusCode
        );
    }

    private void assertResponseMissing(
            JsonNode paths,
            String path,
            String method,
            String statusCode
    ) {

        JsonNode responses = paths
                .path(path)
                .path(method)
                .path("responses");

        assertFalse(
                responses.has(statusCode),
                () -> "Unexpected documented response: "
                        + method.toUpperCase()
                        + " "
                        + path
                        + " status "
                        + statusCode
        );
    }

    private void assertReusableErrorResponse(
            JsonNode documentation,
            String responseName
    ) {

        JsonNode response = documentation
                .path("components")
                .path("responses")
                .path(responseName);

        assertFalse(
                response.isMissingNode(),
                () -> "Missing reusable response: "
                        + responseName
        );

        assertEquals(
                "#/components/schemas/ApiErrorResponse",
                response
                        .path("content")
                        .path("application/json")
                        .path("schema")
                        .path("$ref")
                        .asText(),
                () -> "Unexpected error schema for response: "
                        + responseName
        );
    }

    private void assertJsonRequestBody(
            JsonNode paths,
            String path,
            String method
    ) {

        JsonNode operation = paths
                .path(path)
                .path(method);

        assertFalse(
                operation.isMissingNode(),
                () -> "Missing OpenAPI operation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        JsonNode content = operation
                .path("requestBody")
                .path("content");

        assertTrue(
                content.has("application/json"),
                () -> "Missing application/json request body: "
                        + method.toUpperCase()
                        + " "
                        + path
        );
    }


    private void assertJsonResponse(
            JsonNode paths,
            String path,
            String method,
            String statusCode
    ) {

        JsonNode operation = paths
                .path(path)
                .path(method);

        assertFalse(
                operation.isMissingNode(),
                () -> "Missing OpenAPI operation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        JsonNode content = operation
                .path("responses")
                .path(statusCode)
                .path("content");

        assertTrue(
                content.has("application/json"),
                () -> "Missing application/json response: "
                        + method.toUpperCase()
                        + " "
                        + path
                        + " status "
                        + statusCode
        );
    }

    private void assertOperationSummary(
            JsonNode paths,
            String path,
            String method,
            String expectedSummary
    ) {

        JsonNode operation = paths
                .path(path)
                .path(method);

        assertFalse(
                operation.isMissingNode(),
                () -> "Missing OpenAPI operation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        assertEquals(
                expectedSummary,
                operation.path("summary").asText(),
                () -> "Unexpected OpenAPI summary: "
                        + method.toUpperCase()
                        + " "
                        + path
        );
    }

    private void assertOperationTag(
            JsonNode paths,
            String path,
            String method,
            String expectedTag
    ) {

        JsonNode operation = paths
                .path(path)
                .path(method);

        assertFalse(
                operation.isMissingNode(),
                () -> "Missing OpenAPI operation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        JsonNode tags = operation.path("tags");

        assertTrue(
                tags.isArray() && tags.size() > 0,
                () -> "Missing OpenAPI tag: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        assertEquals(
                expectedTag,
                tags.get(0).asText(),
                () -> "Unexpected OpenAPI tag: "
                        + method.toUpperCase()
                        + " "
                        + path
        );
    }

    private void assertBearerAuth(
            JsonNode paths,
            String path,
            String method
    ) {

        JsonNode operation = paths
                .path(path)
                .path(method);

        assertFalse(
                operation.isMissingNode(),
                () -> "Missing OpenAPI operation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        JsonNode security = operation.path("security");

        assertTrue(
                security.isArray()
                        && security.size() > 0
                        && security.get(0)
                        .has(
                                OpenApiConfig.BEARER_AUTH_SCHEME
                        ),
                () -> "Missing bearerAuth documentation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );
    }

    private void assertPublicOperation(
            JsonNode paths,
            String path,
            String method
    ) {

        JsonNode operation = paths
                .path(path)
                .path(method);

        assertFalse(
                operation.isMissingNode(),
                () -> "Missing OpenAPI operation: "
                        + method.toUpperCase()
                        + " "
                        + path
        );

        assertFalse(
                operation.has("security"),
                () -> "Public operation unexpectedly requires authentication: "
                        + method.toUpperCase()
                        + " "
                        + path
        );
    }
}