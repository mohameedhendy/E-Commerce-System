package com.ecommerce.ecommerce_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME =
            "bearerAuth";

    public static final String BAD_REQUEST_RESPONSE =
            "BadRequest";

    public static final String UNAUTHORIZED_RESPONSE =
            "Unauthorized";

    public static final String FORBIDDEN_RESPONSE =
            "Forbidden";

    public static final String NOT_FOUND_RESPONSE =
            "NotFound";

    public static final String CONFLICT_RESPONSE =
            "Conflict";

    public static final String TOO_MANY_REQUESTS_RESPONSE =
            "TooManyRequests";

    public static final String INTERNAL_SERVER_ERROR_RESPONSE =
            "InternalServerError";

    private static final String API_ERROR_SCHEMA =
            "ApiErrorResponse";

    private static final String API_ERROR_SCHEMA_REFERENCE =
            "#/components/schemas/" + API_ERROR_SCHEMA;

    private static final Set<String> RATE_LIMITED_OPERATION_IDS =
            Set.of(
                    "registerUser",
                    "loginUser",
                    "refreshToken",
                    "verifyEmail",
                    "forgotPassword",
                    "resetPassword"
            );

    private static final Set<String> NOT_FOUND_OPERATION_IDS =
            Set.of(
                    "getProductById",
                    "getProductReviews",
                    "createReview",
                    "updateMyReview",
                    "getAddresses",
                    "addAddress",
                    "updateAddress",
                    "createOrder",
                    "cancelOrder",
                    "getOrderById",
                    "addItem",
                    "updateItemQuantity",
                    "removeItem",
                    "checkout",
                    "getProductByIdForAdmin",
                    "updateProduct",
                    "deleteProduct",
                    "restoreProduct",
                    "updateProductStock",
                    "updateOrderStatus",
                    "getOrderByIdForAdmin",
                    "revokeSession"
            );

    private static final Set<String> CONFLICT_OPERATION_IDS =
            Set.of(
                    "registerUser",
                    "createReview",
                    "createOrder",
                    "checkout",
                    "cancelOrder",
                    "createProduct",
                    "updateProduct",
                    "updateProductStock",
                    "updateOrderStatus"
            );

    @Bean
    public OpenAPI ecommerceOpenApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "E-Commerce Backend API"
                                )
                                .description(
                                        """
                                        REST API for authentication, products,
                                        shopping cart, orders, reviews,
                                        addresses and administration.
                                        """
                                )
                                .version(
                                        "0.0.1-SNAPSHOT"
                                )
                                .contact(
                                        new Contact()
                                                .name(
                                                        "Mohamed Hendy"
                                                )
                                )
                )
                .components(
                        createComponents()
                );
    }

    @Bean
    public OpenApiCustomizer openApiDocumentationCustomizer() {

        return openApi -> {

            addApiErrorSchema(
                    openApi
            );

            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths()
                    .values()
                    .forEach(pathItem ->
                            pathItem.readOperations()
                                    .forEach(
                                            this::addDocumentedErrorResponses
                                    )
                    );
        };
    }

    private void addApiErrorSchema(
            OpenAPI openApi
    ) {

        Components components =
                openApi.getComponents();

        if (components == null) {
            components = new Components();

            openApi.setComponents(
                    components
            );
        }

        components.addSchemas(
                API_ERROR_SCHEMA,
                createApiErrorSchema()
        );
    }

    private void addDocumentedErrorResponses(
            Operation operation
    ) {

        if (hasRequestInput(operation)) {
            addResponseIfAbsent(
                    operation,
                    "400",
                    BAD_REQUEST_RESPONSE
            );
        }

        if (isSecured(operation)) {
            addResponseIfAbsent(
                    operation,
                    "401",
                    UNAUTHORIZED_RESPONSE
            );

            addResponseIfAbsent(
                    operation,
                    "403",
                    FORBIDDEN_RESPONSE
            );
        }

        String operationId =
                operation.getOperationId();

        if (NOT_FOUND_OPERATION_IDS.contains(
                operationId
        )) {
            addResponseIfAbsent(
                    operation,
                    "404",
                    NOT_FOUND_RESPONSE
            );
        }

        if (CONFLICT_OPERATION_IDS.contains(
                operationId
        )) {
            addResponseIfAbsent(
                    operation,
                    "409",
                    CONFLICT_RESPONSE
            );
        }

        if (RATE_LIMITED_OPERATION_IDS.contains(
                operationId
        )) {
            addResponseIfAbsent(
                    operation,
                    "429",
                    TOO_MANY_REQUESTS_RESPONSE
            );
        }
        addResponseIfAbsent(
                operation,
                "500",
                INTERNAL_SERVER_ERROR_RESPONSE
        );
    }

    private boolean hasRequestInput(
            Operation operation
    ) {

        return operation.getRequestBody() != null
                || (
                operation.getParameters() != null
                        && !operation.getParameters().isEmpty()
        );
    }

    private boolean isSecured(
            Operation operation
    ) {

        return operation.getSecurity() != null
                && !operation.getSecurity().isEmpty();
    }

    private void addResponseIfAbsent(
            Operation operation,
            String statusCode,
            String reusableResponse
    ) {

        if (operation.getResponses() == null) {
            operation.setResponses(
                    new ApiResponses()
            );
        }

        operation.getResponses()
                .putIfAbsent(
                        statusCode,
                        new ApiResponse()
                                .$ref(
                                        "#/components/responses/"
                                                + reusableResponse
                                )
                );
    }

    private Components createComponents() {

        return new Components()
                .addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .type(
                                        SecurityScheme.Type.HTTP
                                )
                                .scheme(
                                        "bearer"
                                )
                                .bearerFormat(
                                        "JWT"
                                )
                )
                .addResponses(
                        BAD_REQUEST_RESPONSE,
                        createErrorResponse(
                                "The request is invalid"
                        )
                )
                .addResponses(
                        UNAUTHORIZED_RESPONSE,
                        createErrorResponse(
                                "Authentication is required or has failed"
                        )
                )
                .addResponses(
                        FORBIDDEN_RESPONSE,
                        createErrorResponse(
                                "The authenticated user cannot perform this action"
                        )
                )
                .addResponses(
                        NOT_FOUND_RESPONSE,
                        createErrorResponse(
                                "The requested resource was not found"
                        )
                )
                .addResponses(
                        CONFLICT_RESPONSE,
                        createErrorResponse(
                                "The request conflicts with the current resource state"
                        )
                )
                .addResponses(
                        TOO_MANY_REQUESTS_RESPONSE,
                        createErrorResponse(
                                "The authentication request rate limit was exceeded"
                        )
                )                .addResponses(
                        INTERNAL_SERVER_ERROR_RESPONSE,
                        createErrorResponse(
                                "An unexpected server error occurred"
                        )
                );
    }

    private Schema<?> createApiErrorSchema() {

        ObjectSchema validationErrorsSchema =
                new ObjectSchema();

        return new ObjectSchema()
                .addProperty(
                        "timestamp",
                        new StringSchema()
                                .format(
                                        "date-time"
                                )
                )
                .addProperty(
                        "status",
                        new IntegerSchema()
                                .format(
                                        "int32"
                                )
                )
                .addProperty(
                        "error",
                        new StringSchema()
                )
                .addProperty(
                        "message",
                        new StringSchema()
                )
                .addProperty(
                        "validationErrors",
                        validationErrorsSchema
                );
    }

    private ApiResponse createErrorResponse(
            String description
    ) {

        Schema<?> errorSchema =
                new Schema<>()
                        .$ref(
                                API_ERROR_SCHEMA_REFERENCE
                        );

        MediaType mediaType =
                new MediaType()
                        .schema(
                                errorSchema
                        );

        Content content =
                new Content()
                        .addMediaType(
                                org.springframework.http.MediaType
                                        .APPLICATION_JSON_VALUE,
                                mediaType
                        );

        return new ApiResponse()
                .description(
                        description
                )
                .content(
                        content
                );
    }
}