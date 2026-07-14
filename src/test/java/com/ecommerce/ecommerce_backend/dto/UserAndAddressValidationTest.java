package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserAndAddressValidationTest {

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
    public void firstNameLongerThanLimitFailsValidation() {

        RegistrationBody body =
                createValidRegistrationBody();

        body.setFirstName("a".repeat(256));

        Assertions.assertTrue(
                hasViolation(
                        body,
                        "firstName",
                        "First name must not exceed 255 characters"
                )
        );
    }

    @Test
    public void lastNameLongerThanLimitFailsValidation() {

        RegistrationBody body =
                createValidRegistrationBody();

        body.setLastName("a".repeat(256));

        Assertions.assertTrue(
                hasViolation(
                        body,
                        "lastName",
                        "Last name must not exceed 255 characters"
                )
        );
    }

    @Test
    public void emailLongerThanLimitFailsValidation() {

        RegistrationBody body =
                createValidRegistrationBody();

        body.setEmail(
                "a".repeat(309) + "@example.com"
        );

        Assertions.assertTrue(
                hasViolation(
                        body,
                        "email",
                        "Email must not exceed 320 characters"
                )
        );
    }

    @Test
    public void registrationNamesAtMaximumLengthPassValidation() {

        RegistrationBody body =
                createValidRegistrationBody();

        body.setFirstName("a".repeat(255));
        body.setLastName("b".repeat(255));

        Assertions.assertTrue(
                validator.validate(body).isEmpty(),
                "Names with exactly 255 characters should be accepted."
        );
    }

    @Test
    public void cityLongerThanLimitFailsValidation() {

        AddressRequest request =
                createValidAddressRequest();

        request.setCity("a".repeat(256));

        Assertions.assertTrue(
                hasViolation(
                        request,
                        "city",
                        "City must not exceed 255 characters"
                )
        );
    }

    @Test
    public void addressFieldsAtMaximumLengthsPassValidation() {

        AddressRequest request =
                createValidAddressRequest();

        request.setAddressLine1("a".repeat(512));
        request.setAddressLine2("b".repeat(512));
        request.setCountry("c".repeat(75));
        request.setCity("d".repeat(255));

        Assertions.assertTrue(
                validator.validate(request).isEmpty(),
                "Address fields at their maximum lengths should be accepted."
        );
    }

    private RegistrationBody createValidRegistrationBody() {

        RegistrationBody body =
                new RegistrationBody();

        body.setUsername("ValidationUser");
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setEmail("validation-user@junit.com");
        body.setPassword("Password123!");

        return body;
    }

    private AddressRequest createValidAddressRequest() {

        AddressRequest request =
                new AddressRequest();

        request.setAddressLine1("123 Test Street");
        request.setAddressLine2("Apartment 10");
        request.setCountry("Egypt");
        request.setCity("Cairo");

        return request;
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
                        violation
                                .getPropertyPath()
                                .toString()
                                .equals(field)
                                && violation
                                .getMessage()
                                .equals(message)
                );
    }
}