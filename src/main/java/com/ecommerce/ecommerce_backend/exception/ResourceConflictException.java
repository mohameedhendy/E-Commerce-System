package com.ecommerce.ecommerce_backend.exception;

public class ResourceConflictException
        extends RuntimeException {

    public ResourceConflictException(
            String message
    ) {
        super(message);
    }
}