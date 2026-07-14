package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.VerificationTokenDAO;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.PasswordResetBody;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.exception.InvalidCredentialsException;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistException;
import com.ecommerce.ecommerce_backend.exception.UserNotVerifiedException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.VerificationToken;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    /**
     * Extension for mocking email sending.
     */
    @RegisterExtension
    private static final GreenMailExtension greenMailExtension =
            new GreenMailExtension(ServerSetupTest.SMTP)
                    .withConfiguration(
                            GreenMailConfiguration
                                    .aConfig()
                                    .withUser("springboot", "secret")
                    )
                    .withPerMethodLifecycle(true);

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenDAO verificationTokenDAO;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private EncryptionService encryptionService;

    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {

        RegistrationBody body = new RegistrationBody();
        body.setUsername("UserA");
        body.setEmail(
                "UserServiceTest$testRegisterUser@junit.com"
        );
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setPassword("MySecretPassword123");

        Assertions.assertThrows(
                UserAlreadyExistException.class,
                () -> userService.registerUser(body),
                "Username should already be in use."
        );

        body.setUsername(
                "UserServiceTest$testRegisterUser"
        );
        body.setEmail("UserA@junit.com");

        Assertions.assertThrows(
                UserAlreadyExistException.class,
                () -> userService.registerUser(body),
                "Email should already be in use."
        );

        body.setEmail(
                "UserServiceTest$testRegisterUser@junit.com"
        );

        Assertions.assertDoesNotThrow(
                () -> userService.registerUser(body),
                "User should register successfully."
        );

        Assertions.assertEquals(
                body.getEmail(),
                greenMailExtension
                        .getReceivedMessages()[0]
                        .getRecipients(
                                Message.RecipientType.TO
                        )[0]
                        .toString()
        );
    }

    @Test
    @Transactional
    public void testLoginUser()
            throws UserNotVerifiedException,
            EmailFailureException {

        LoginBody body = new LoginBody();

        body.setUsername("UserA");
        body.setPassword("PasswordA123");

        Assertions.assertNotNull(
                userService.loginUser(body),
                "The user should login successfully."
        );

        body.setUsername("UserB");
        body.setPassword("PasswordB123");

        UserNotVerifiedException firstAttemptException =
                Assertions.assertThrows(
                        UserNotVerifiedException.class,
                        () -> userService.loginUser(body),
                        "User should not have email verified."
                );

        Assertions.assertTrue(
                firstAttemptException.isNewEmailSend(),
                "Email verification should be sent."
        );

        Assertions.assertEquals(
                1,
                greenMailExtension
                        .getReceivedMessages()
                        .length
        );

        UserNotVerifiedException secondAttemptException =
                Assertions.assertThrows(
                        UserNotVerifiedException.class,
                        () -> userService.loginUser(body),
                        "User should still not have email verified."
                );

        Assertions.assertFalse(
                secondAttemptException.isNewEmailSend(),
                "Email verification should not be resent."
        );

        Assertions.assertEquals(
                1,
                greenMailExtension
                        .getReceivedMessages()
                        .length
        );
    }

    @Test
    @Transactional
    public void testVerifyUser()
            throws EmailFailureException {

        Assertions.assertFalse(
                userService.verifyUser("Bad Token"),
                "Token that is bad or does not exist should return false."
        );

        LocalUser unverifiedUser = localUserDao
                .findByUsernameIgnoreCase("UserB")
                .orElseThrow();

        LoginBody body = new LoginBody();
        body.setUsername(unverifiedUser.getUsername());
        body.setPassword("PasswordB123");

        Assertions.assertThrows(
                UserNotVerifiedException.class,
                () -> userService.loginUser(body),
                "User should not have email verified."
        );

        List<VerificationToken> tokens =
                verificationTokenDAO
                        .findByUser_IdOrderByIdDesc(
                                unverifiedUser.getId()
                        );

        Assertions.assertFalse(
                tokens.isEmpty(),
                "A verification token should be created."
        );

        String token = tokens
                .getFirst()
                .getToken();

        Assertions.assertTrue(
                userService.verifyUser(token),
                "Token should be valid."
        );

        LocalUser verifiedUser = localUserDao
                .findByUsernameIgnoreCase("UserB")
                .orElseThrow();

        Assertions.assertTrue(
                verifiedUser.isEmailVerified(),
                "The user should now be verified."
        );

        List<VerificationToken> remainingTokens =
                verificationTokenDAO
                        .findByUser_IdOrderByIdDesc(
                                verifiedUser.getId()
                        );

        Assertions.assertTrue(
                remainingTokens.isEmpty(),
                "Verification tokens should be deleted after successful verification."
        );
    }

    @Test
    @Transactional
    public void testForgotPassword()
            throws MessagingException {

        Assertions.assertDoesNotThrow(
                () -> userService.forgotPassword(
                        "UserNotExist@junit.com"
                ),
                "Unknown email should not reveal whether an account exists."
        );

        Assertions.assertEquals(
                0,
                greenMailExtension
                        .getReceivedMessages()
                        .length,
                "No email should be sent for an unknown address."
        );

        Assertions.assertDoesNotThrow(
                () -> userService.forgotPassword(
                        "UserA@junit.com"
                ),
                "Existing email should be accepted."
        );

        Assertions.assertEquals(
                1,
                greenMailExtension
                        .getReceivedMessages()
                        .length,
                "A password reset email should be sent for an existing account."
        );

        Assertions.assertEquals(
                "UserA@junit.com",
                greenMailExtension
                        .getReceivedMessages()[0]
                        .getRecipients(
                                Message.RecipientType.TO
                        )[0]
                        .toString()
        );
    }

    @Test
    @Transactional
    public void testResetPassword()
            throws InvalidTokenException {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        long originalResetVersion =
                user.getPasswordResetVersion();

        String token =
                jwtService.generatePasswordResetJWT(user);

        PasswordResetBody body =
                new PasswordResetBody();

        body.setToken(token);
        body.setPassword("Password123456");

        userService.resetPassword(body);

        LocalUser updatedUser = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Assertions.assertTrue(
                encryptionService.verifyPassword(
                        "Password123456",
                        updatedUser.getPassword()
                ),
                "Password change should be written to DB."
        );

        Assertions.assertEquals(
                originalResetVersion + 1,
                updatedUser.getPasswordResetVersion(),
                "Password reset version should increase after reset."
        );

        Assertions.assertThrows(
                InvalidTokenException.class,
                () -> userService.resetPassword(body),
                "Password reset token must not be reusable."
        );
    }

    @Test
    @Transactional
    public void invalidPasswordThrowsInvalidCredentialsException() {

        LoginBody loginBody = new LoginBody();

        loginBody.setUsername("UserA");
        loginBody.setPassword(
                "DefinitelyWrongPassword123"
        );

        InvalidCredentialsException exception =
                Assertions.assertThrows(
                        InvalidCredentialsException.class,
                        () -> userService.loginUser(
                                loginBody
                        )
                );

        Assertions.assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );
    }

    @Test
    @Transactional
    public void unknownUsernameThrowsInvalidCredentialsException() {

        LoginBody loginBody = new LoginBody();

        loginBody.setUsername(
                "UserDoesNotExist"
        );
        loginBody.setPassword("Password123");

        Assertions.assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(
                        loginBody
                )
        );
    }
}