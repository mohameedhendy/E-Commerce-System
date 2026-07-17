package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.VerificationTokenDAO;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.LoginResponse;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "app.email.verification.enabled=false"
        }
)
@Transactional
public class UserServiceEmailVerificationDisabledTest {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenDAO verificationTokenDAO;

    @Test
    public void explicitDisableMarksUserVerifiedAndAllowsLogin() {

        RegistrationBody registrationBody =
                new RegistrationBody();

        registrationBody.setUsername(
                "verificationDisabledUser"
        );

        registrationBody.setFirstName(
                "Verification"
        );

        registrationBody.setLastName(
                "Disabled"
        );

        registrationBody.setEmail(
                "verification-disabled-user@junit.com"
        );

        registrationBody.setPassword(
                "SecurePassword123"
        );

        LocalUser registeredUser =
                Assertions.assertDoesNotThrow(
                        () -> userService.registerUser(
                                registrationBody
                        )
                );

        Assertions.assertTrue(
                registeredUser.isEmailVerified(),
                "An explicit disabled override should mark the user as verified."
        );

        Assertions.assertTrue(
                verificationTokenDAO
                        .findByUser_IdOrderByIdDesc(
                                registeredUser.getId()
                        )
                        .isEmpty(),
                "No verification token should be created when verification is explicitly disabled."
        );

        LoginBody loginBody =
                new LoginBody();

        loginBody.setUsername(
                registrationBody.getUsername()
        );

        loginBody.setPassword(
                registrationBody.getPassword()
        );

        LoginResponse loginResponse =
                Assertions.assertDoesNotThrow(
                        () -> userService.loginUser(
                                loginBody
                        )
                );

        Assertions.assertNotNull(
                loginResponse.getAccessToken(),
                "Explicit disabled mode should allow login after registration."
        );

        Assertions.assertNotNull(
                loginResponse.getRefreshToken(),
                "Login should still create a refresh session."
        );
    }
}
