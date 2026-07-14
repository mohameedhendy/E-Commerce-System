package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CartRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }

    @AfterAll
    public static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    public void validAddCartItemRequestPassesValidation() {

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(1L);
        request.setQuantity(2);

        Assertions.assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    public void missingProductIdFailsValidation() {

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setQuantity(2);

        Assertions.assertTrue(
                hasViolation(
                        request,
                        "productId",
                        "Product id is required"
                )
        );
    }

    @Test
    public void zeroQuantityFailsAddValidation() {

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(1L);
        request.setQuantity(0);

        Assertions.assertTrue(
                hasViolation(
                        request,
                        "quantity",
                        "Quantity must be at least 1"
                )
        );
    }

    @Test
    public void negativeQuantityFailsAddValidation() {

        AddCartItemRequest request =
                new AddCartItemRequest();

        request.setProductId(1L);
        request.setQuantity(-1);

        Assertions.assertTrue(
                hasViolation(
                        request,
                        "quantity",
                        "Quantity must be at least 1"
                )
        );
    }

    @Test
    public void missingQuantityFailsUpdateValidation() {

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        Assertions.assertTrue(
                hasViolation(
                        request,
                        "quantity",
                        "Quantity is required"
                )
        );
    }

    @Test
    public void zeroQuantityFailsUpdateValidation() {

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(0);

        Assertions.assertTrue(
                hasViolation(
                        request,
                        "quantity",
                        "Quantity must be at least 1"
                )
        );
    }

    @Test
    public void validUpdateQuantityPassesValidation() {

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(5);

        Assertions.assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    private <T> boolean hasViolation(
            T object,
            String field,
            String message
    ) {
        return validator
                .validate(object)
                .stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals(field)
                                && violation.getMessage()
                                .equals(message)
                );
    }
}