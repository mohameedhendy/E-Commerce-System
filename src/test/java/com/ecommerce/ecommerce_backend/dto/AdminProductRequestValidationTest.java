package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class AdminProductRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    public void validProductDetailsRequestPassesValidation() {

        AdminProductRequest request =
                createValidRequest();

        Assertions.assertTrue(
                validator.validate(request).isEmpty(),
                "Valid product details should pass validation."
        );
    }

    @Test
    public void productDetailsRequestDoesNotContainStockQuantity() {

        boolean containsStockQuantity =
                Arrays.stream(
                                AdminProductRequest.class
                                        .getDeclaredFields()
                        )
                        .anyMatch(field ->
                                field.getName()
                                        .equals("stockQuantity")
                        );

        Assertions.assertFalse(
                containsStockQuantity,
                "Product details request must not expose stock quantity."
        );
    }

    @Test
    public void productNameLongerThanLimitFailsValidation() {

        AdminProductRequest request =
                createValidRequest();

        request.setName("a".repeat(256));

        Assertions.assertFalse(
                validator.validate(request).isEmpty(),
                "Product name longer than 255 characters should be rejected."
        );
    }

    @Test
    public void shortDescriptionLongerThanLimitFailsValidation() {

        AdminProductRequest request =
                createValidRequest();

        request.setShortDescription(
                "a".repeat(256)
        );

        Assertions.assertFalse(
                validator.validate(request).isEmpty(),
                "Short description longer than 255 characters should be rejected."
        );
    }

    @Test
    public void longDescriptionLongerThanLimitFailsValidation() {

        AdminProductRequest request =
                createValidRequest();

        request.setLongDescription(
                "a".repeat(256)
        );

        Assertions.assertFalse(
                validator.validate(request).isEmpty(),
                "Long description longer than 255 characters should be rejected."
        );
    }

    @Test
    public void productTextAtMaximumLengthPassesValidation() {

        AdminProductRequest request =
                createValidRequest();

        request.setName("a".repeat(255));
        request.setShortDescription(
                "a".repeat(255)
        );
        request.setLongDescription(
                "a".repeat(255)
        );

        Assertions.assertTrue(
                validator.validate(request).isEmpty(),
                "Product text with exactly 255 characters should be accepted."
        );
    }

    private AdminProductRequest createValidRequest() {

        AdminProductRequest request =
                new AdminProductRequest();

        request.setName("Test Product");

        request.setShortDescription(
                "Short product description"
        );

        request.setLongDescription(
                "Long product description"
        );

        request.setPrice(
                new BigDecimal("99.99")
        );

        return request;
    }
}
