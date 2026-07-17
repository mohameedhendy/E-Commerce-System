package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

public class AdminProductCreateRequestValidationTest {

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
    public void validCreateRequestPassesValidation() {

        AdminProductCreateRequest request =
                createValidRequest();

        Assertions.assertTrue(
                validator.validate(request).isEmpty(),
                "Valid product creation request should pass validation."
        );
    }

    @Test
    public void missingInitialStockFailsValidation() {

        AdminProductCreateRequest request =
                createValidRequest();

        request.setStockQuantity(null);

        Set<ConstraintViolation<AdminProductCreateRequest>> violations =
                validator.validate(request);

        Assertions.assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals("stockQuantity")
                        ),
                "Initial stock quantity should be required."
        );
    }

    @Test
    public void negativeInitialStockFailsValidation() {

        AdminProductCreateRequest request =
                createValidRequest();

        request.setStockQuantity(-1);

        Assertions.assertFalse(
                validator.validate(request).isEmpty(),
                "Negative initial stock should be rejected."
        );
    }

    @Test
    public void zeroInitialStockPassesValidation() {

        AdminProductCreateRequest request =
                createValidRequest();

        request.setStockQuantity(0);

        Assertions.assertTrue(
                validator.validate(request).isEmpty(),
                "A product may be created with zero stock."
        );
    }

    private AdminProductCreateRequest createValidRequest() {

        AdminProductCreateRequest request =
                new AdminProductCreateRequest();

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

        request.setStockQuantity(10);

        return request;
    }
}
