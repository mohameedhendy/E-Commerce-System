package com.ecommerce.ecommerce_backend.exception;

public class InvalidTokenException extends Exception {

    public InvalidTokenException(String message) {
        super(message);
    }
}