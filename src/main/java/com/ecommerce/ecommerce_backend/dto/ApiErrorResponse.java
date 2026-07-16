package com.ecommerce.ecommerce_backend.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {

    public ApiErrorResponse(
            int status,
            String error,
            String message
    ) {
        this(
                Instant.now(),
                status,
                error,
                message,
                Map.of()
        );
    }

    public ApiErrorResponse(
            int status,
            String error,
            String message,
            Map<String, String> validationErrors
    ) {
        this(
                Instant.now(),
                status,
                error,
                message,
                validationErrors
        );
    }

    public ApiErrorResponse {

        if (timestamp == null) {
            timestamp = Instant.now();
        }

        validationErrors =
                immutableValidationErrors(
                        validationErrors
                );
    }

    private static Map<String, String>
    immutableValidationErrors(
            Map<String, String> validationErrors
    ) {

        if (validationErrors == null
                || validationErrors.isEmpty()) {

            return Map.of();
        }

        Map<String, String> safeErrors =
                new LinkedHashMap<>();

        validationErrors.forEach(
                (field, message) ->
                        safeErrors.put(
                                field,
                                message == null
                                        ? "Invalid value"
                                        : message
                        )
        );

        return Collections.unmodifiableMap(
                safeErrors
        );
    }
}