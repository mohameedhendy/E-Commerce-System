package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.VerificationTokenDAO;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.PasswordResetBody;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.exception.*;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.VerificationToken;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    /**
     * Extension for mocking email sending.
     */
    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
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
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setPassword("MySecretPassword123");
        Assertions.assertThrows(UserAlreadyExistException.class,
                () -> userService.registerUser(body), "Username should already be in use.");
        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("UserA@junit.com");
        Assertions.assertThrows(UserAlreadyExistException.class,
                () -> userService.registerUser(body), "Email should already be in use.");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> userService.registerUser(body),
                "User should register successfully.");
        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    @Transactional
    public void testLoginUser()
            throws UserNotVerifiedException, EmailFailureException {

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
                greenMailExtension.getReceivedMessages().length
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
                greenMailExtension.getReceivedMessages().length
        );
    }

    @Test
    @Transactional
    public void testVerifyUser() throws EmailFailureException {
        Assertions.assertFalse(userService.verifyUser("Bad Token"), "Token that is bad or does not exist should return false.");
        LoginBody body = new LoginBody();
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try {
            userService.loginUser(body);
            Assertions.assertTrue(false, "User should not have email verified.");
        } catch (UserNotVerifiedException ex) {
            List<VerificationToken> tokens = verificationTokenDAO.findByUser_IdOrderByIdDesc(2L);
            String token = tokens.getFirst().getToken();
            Assertions.assertTrue(userService.verifyUser(token), "Token should be valid.");
            Assertions.assertNotNull(body, "The user should now be verified.");
        }
    }

    @Test
    @Transactional
    public void testForgotPassword() throws MessagingException {
        Assertions.assertThrows(EmailNotFoundException.class,
                () -> userService.forgotPassword("UserNotExist@junit.com"));
        Assertions.assertDoesNotThrow(() -> userService.forgotPassword("UserA@junit.com"),
                "Non existing email should be rejected.");
        Assertions.assertEquals("UserA@junit.com",
                greenMailExtension.getReceivedMessages()[0]
                        .getRecipients(Message.RecipientType.TO)[0].toString(), "Password " +
                        "reset email should be sent.");
    }

    @Test
    public void testResetPassword() throws InvalidTokenException {
        LocalUser user = localUserDao.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        PasswordResetBody body = new PasswordResetBody();
        body.setToken(token);
        body.setPassword("Password123456");
        userService.resetPassword(body);
        user = localUserDao.findByUsernameIgnoreCase("UserA").get();
        Assertions.assertTrue(encryptionService.verifyPassword("Password123456",
                user.getPassword()), "Password change should be written to DB.");
    }

    @Test
    public void invalidPasswordThrowsInvalidCredentialsException() {

        LoginBody loginBody = new LoginBody();

        loginBody.setUsername("UserA");
        loginBody.setPassword("DefinitelyWrongPassword123");

        InvalidCredentialsException exception =
                Assertions.assertThrows(
                        InvalidCredentialsException.class,
                        () -> userService.loginUser(loginBody)
                );

        Assertions.assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );
    }

    @Test
    public void unknownUsernameThrowsInvalidCredentialsException() {

        LoginBody loginBody = new LoginBody();

        loginBody.setUsername("UserDoesNotExist");
        loginBody.setPassword("Password123");

        Assertions.assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(loginBody)
        );
    }
}
