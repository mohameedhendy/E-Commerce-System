package com.ecommerce.ecommerce_backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ecommerce.ecommerce_backend.config.JwtProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    public void testAuthTokenReturnsUsername() {
        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String token = jwtService.generateToken(user);

        Assertions.assertEquals(
                user.getUsername(),
                jwtService.getUsername(token),
                "Access token should contain the user's username."
        );
    }

    @Test
    public void testVerificationTokenNotUsableForLogin() {
        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String token =
                jwtService.generateVerificationJWT(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getUsername(token),
                "Email verification token must not work as an access token."
        );
    }

    @Test
    public void testPasswordResetTokenNotUsableForLogin() {
        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String token =
                jwtService.generatePasswordResetJWT(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getUsername(token),
                "Password reset token must not work as an access token."
        );
    }

    @Test
    public void testAccessTokenNotUsableForPasswordReset() {
        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String token = jwtService.generateToken(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getResetPasswordEmail(token),
                "Access token must not work as a password reset token."
        );
    }

    @Test
    public void testAccessTokenNotUsableForEmailVerification() {
        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String token = jwtService.generateToken(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getVerificationEmail(token),
                "Access token must not work as an email verification token."
        );
    }

    @Test
    public void testLoginJWTNotGeneratedByUs() {
        String token = JWT.create()
                .withClaim("USERNAME", "UserA")
                .sign(
                        Algorithm.HMAC256(
                                "NotTheRealSecret"
                        )
                );

        Assertions.assertThrows(
                SignatureVerificationException.class,
                () -> jwtService.getUsername(token)
        );
    }

    @Test
    public void testLoginJWTCorrectlySignedNoIssuer() {
        String token = JWT.create()
                .withClaim("USERNAME", "UserA")
                .withClaim("TOKEN_TYPE", "ACCESS")
                .sign(Algorithm.HMAC256(
        jwtProperties.algorithm().key()
));

        Assertions.assertThrows(
                MissingClaimException.class,
                () -> jwtService.getUsername(token)
        );
    }

    @Test
    public void testResetPasswordJWTNotGeneratedByUs() {
        String token = JWT.create()
                .withClaim(
                        "RESET_PASSWORD_EMAIL",
                        "UserA@junit.com"
                )
                .withClaim(
                        "TOKEN_TYPE",
                        "PASSWORD_RESET"
                )
                .sign(
                        Algorithm.HMAC256(
                                "NotTheRealSecret"
                        )
                );

        Assertions.assertThrows(
                SignatureVerificationException.class,
                () -> jwtService.getResetPasswordEmail(token)
        );
    }

    @Test
    public void testResetPasswordJWTCorrectlySignedNoIssuer() {
        String token = JWT.create()
                .withClaim(
                        "RESET_PASSWORD_EMAIL",
                        "UserA@junit.com"
                )
                .withClaim(
                        "TOKEN_TYPE",
                        "PASSWORD_RESET"
                )
                .sign(Algorithm.HMAC256(
        jwtProperties.algorithm().key()
));

        Assertions.assertThrows(
                MissingClaimException.class,
                () -> jwtService.getResetPasswordEmail(token)
        );
    }

    @Test
    public void testPasswordResetToken() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String token =
                jwtService.generatePasswordResetJWT(user);

        JWTService.PasswordResetTokenData tokenData =
                jwtService.getPasswordResetData(token);

        Assertions.assertEquals(
                user.getEmail(),
                tokenData.email(),
                "Password reset token should contain the user's email."
        );

        Assertions.assertEquals(
                user.getPasswordResetVersion(),
                tokenData.version(),
                "Password reset token should contain the current reset version."
        );
    }
}