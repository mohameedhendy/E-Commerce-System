package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PasswordPolicyValidationTest {

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
    public void registrationPasswordAllowsSpecialCharacters() {

        RegistrationBody body =
                createValidRegistrationBody();

        body.setPassword("Password123!");

        Assertions.assertTrue(
                validator.validate(body).isEmpty(),
                "Registration password should allow special characters."
        );
    }

    @Test
    public void passwordResetAllowsSpecialCharacters() {

        PasswordResetBody body =
                new PasswordResetBody();

        body.setToken("valid-non-blank-token");
        body.setPassword("Password123!");

        Assertions.assertTrue(
                validator.validate(body).isEmpty(),
                "Password reset should allow special characters."
        );
    }

    @Test
    public void loginDoesNotApplyPasswordCreationPolicy() {

        LoginBody body = new LoginBody();

        body.setUsername("UserA");
        body.setPassword("legacy-password");

        Assertions.assertTrue(
                validator.validate(body).isEmpty(),
                "Login should only require a non-blank password."
        );
    }

    @Test
    public void registrationPasswordStillRequiresNumber() {

        RegistrationBody body =
                createValidRegistrationBody();

        body.setPassword("Password!");

        Assertions.assertFalse(
                validator.validate(body).isEmpty(),
                "Registration password should require a number."
        );
    }

    @Test
    public void resetPasswordStillRequiresLetter() {

        PasswordResetBody body =
                new PasswordResetBody();

        body.setToken("valid-non-blank-token");
        body.setPassword("12345678!");

        Assertions.assertFalse(
                validator.validate(body).isEmpty(),
                "Reset password should require a letter."
        );
    }

    private RegistrationBody createValidRegistrationBody() {

        RegistrationBody body =
                new RegistrationBody();

        body.setUsername("PasswordPolicyUser");
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setEmail("password-policy@junit.com");
        body.setPassword("Password123");

        return body;
    }
}