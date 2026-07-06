package com.ecommerce.ecommerce_backend.exception;

public class InvalidProductStatusException extends RuntimeException {

    public InvalidProductStatusException(String message) {
        super(message);
    }
}