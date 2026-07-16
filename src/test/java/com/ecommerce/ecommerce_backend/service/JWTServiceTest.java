package com.ecommerce.ecommerce_backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.ecommerce_backend.config.JwtProperties;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

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

        String token =
                jwtService.generateToken(user);

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

        String token =
                jwtService.generateToken(user);

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

        String token =
                jwtService.generateToken(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getVerificationEmail(token),
                "Access token must not work as an email verification token."
        );
    }

    @Test
    public void testLoginJWTNotGeneratedByUs() {

        String token =
                JWT.create()
                        .withClaim(
                                "USERNAME",
                                "UserA"
                        )
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
    public void testLoginJWTRequiresIssuerAndAudience() {

        String tokenWithoutIssuer =
                JWT.create()
                        .withClaim(
                                "USERNAME",
                                "UserA"
                        )
                        .withClaim(
                                "TOKEN_TYPE",
                                "ACCESS"
                        )
                        .withAudience(
                                jwtProperties.audience()
                        )
                        .sign(
                                Algorithm.HMAC256(
                                        jwtProperties
                                                .algorithm()
                                                .key()
                                )
                        );

        Assertions.assertThrows(
                MissingClaimException.class,
                () -> jwtService.getUsername(
                        tokenWithoutIssuer
                ),
                "Access token must contain the configured issuer."
        );

        String tokenWithWrongAudience =
                JWT.create()
                        .withClaim(
                                "USERNAME",
                                "UserA"
                        )
                        .withClaim(
                                "TOKEN_TYPE",
                                "ACCESS"
                        )
                        .withIssuer(
                                jwtProperties.issuer()
                        )
                        .withAudience(
                                "different-service"
                        )
                        .sign(
                                Algorithm.HMAC256(
                                        jwtProperties
                                                .algorithm()
                                                .key()
                                )
                        );

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getUsername(
                        tokenWithWrongAudience
                ),
                "Access token must target the configured audience."
        );
    }

    @Test
    public void testResetPasswordJWTNotGeneratedByUs() {

        String token =
                JWT.create()
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

        String token =
                JWT.create()
                        .withClaim(
                                "RESET_PASSWORD_EMAIL",
                                "UserA@junit.com"
                        )
                        .withClaim(
                                "TOKEN_TYPE",
                                "PASSWORD_RESET"
                        )
                        .withAudience(
                                jwtProperties.audience()
                        )
                        .sign(
                                Algorithm.HMAC256(
                                        jwtProperties
                                                .algorithm()
                                                .key()
                                )
                        );

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

    @Test
    public void accessTokenUsesConfiguredExpiryTime() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        long beforeGeneration =
                System.currentTimeMillis() / 1000L;

        String token =
                jwtService.generateToken(user);

        long afterGeneration =
                System.currentTimeMillis() / 1000L;

        DecodedJWT decodedJWT =
                JWT.decode(token);

        Assertions.assertNotNull(
                decodedJWT.getExpiresAt()
        );

        long tokenExpirySeconds =
                decodedJWT
                        .getExpiresAt()
                        .getTime()
                        / 1000L;

        long configuredExpirySeconds =
                jwtProperties.expiryInSeconds();

        Assertions.assertTrue(
                tokenExpirySeconds
                        >= beforeGeneration
                        + configuredExpirySeconds
        );

        Assertions.assertTrue(
                tokenExpirySeconds
                        <= afterGeneration
                        + configuredExpirySeconds
        );
    }

    @Test
    public void refreshTokenReturnsUsername() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String refreshToken =
                generateSessionRefreshToken(user);

        Assertions.assertEquals(
                user.getUsername(),
                jwtService.getRefreshUsername(
                        refreshToken
                )
        );
    }

    @Test
    public void refreshTokenCannotBeUsedAsAccessToken() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String refreshToken =
                generateSessionRefreshToken(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getUsername(
                        refreshToken
                ),
                "Refresh token must not work as an access token."
        );
    }

    @Test
    public void accessTokenCannotBeUsedAsRefreshToken() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String accessToken =
                jwtService.generateToken(user);

        Assertions.assertThrows(
                JWTVerificationException.class,
                () -> jwtService.getRefreshUsername(
                        accessToken
                ),
                "Access token must not work as a refresh token."
        );
    }

    @Test
    public void refreshTokenUsesConfiguredExpiryTime() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        long beforeGeneration =
                System.currentTimeMillis() / 1000L;

        String refreshToken =
                generateSessionRefreshToken(user);

        long afterGeneration =
                System.currentTimeMillis() / 1000L;

        DecodedJWT decodedJWT =
                JWT.decode(refreshToken);

        Assertions.assertNotNull(
                decodedJWT.getExpiresAt()
        );

        long tokenExpirySeconds =
                decodedJWT
                        .getExpiresAt()
                        .getTime()
                        / 1000L;

        long configuredExpirySeconds =
                jwtProperties.refreshExpiryInSeconds();

        Assertions.assertTrue(
                tokenExpirySeconds
                        >= beforeGeneration
                        + configuredExpirySeconds
        );

        Assertions.assertTrue(
                tokenExpirySeconds
                        <= afterGeneration
                        + configuredExpirySeconds
        );
    }

    @Test
    public void refreshTokenContainsCurrentVersion() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String refreshToken =
                generateSessionRefreshToken(user);

        JWTService.RefreshTokenData tokenData =
                jwtService.getRefreshTokenData(
                        refreshToken
                );

        Assertions.assertEquals(
                user.getUsername(),
                tokenData.username()
        );

        Assertions.assertEquals(
                user.getRefreshTokenVersion(),
                tokenData.version()
        );
    }

    @Test
    public void sessionRefreshTokenContainsSessionData() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String sessionId =
                "ee885780-fbd7-4d2a-8055-a30d37966ecf";

        long sessionVersion = 4L;

        String refreshToken =
                jwtService.generateRefreshToken(
                        user,
                        sessionId,
                        sessionVersion
                );

        JWTService.RefreshTokenData tokenData =
                jwtService.getRefreshTokenData(
                        refreshToken
                );

        Assertions.assertEquals(
                user.getUsername(),
                tokenData.username()
        );

        Assertions.assertEquals(
                user.getRefreshTokenVersion(),
                tokenData.version()
        );

        Assertions.assertEquals(
                sessionId,
                tokenData.sessionId()
        );

        Assertions.assertEquals(
                sessionVersion,
                tokenData.sessionVersion()
        );
    }

    @Test
    public void separateRefreshSessionsKeepIndependentSessionData() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        String firstSessionId =
                "18aee42d-b379-43e7-9e75-3fe83f31ba43";

        String secondSessionId =
                "5fe9fc5b-cb12-490c-b250-f37f5a645b32";

        String firstToken =
                jwtService.generateRefreshToken(
                        user,
                        firstSessionId,
                        0L
                );

        String secondToken =
                jwtService.generateRefreshToken(
                        user,
                        secondSessionId,
                        7L
                );

        JWTService.RefreshTokenData firstTokenData =
                jwtService.getRefreshTokenData(
                        firstToken
                );

        JWTService.RefreshTokenData secondTokenData =
                jwtService.getRefreshTokenData(
                        secondToken
                );

        Assertions.assertEquals(
                firstSessionId,
                firstTokenData.sessionId()
        );

        Assertions.assertEquals(
                0L,
                firstTokenData.sessionVersion()
        );

        Assertions.assertEquals(
                secondSessionId,
                secondTokenData.sessionId()
        );

        Assertions.assertEquals(
                7L,
                secondTokenData.sessionVersion()
        );

        Assertions.assertNotEquals(
                firstTokenData.sessionId(),
                secondTokenData.sessionId(),
                "Different login sessions must retain independent session IDs."
        );
    }

    @Test
    public void verificationTokenUsesConfiguredExpiryTime() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        long beforeGeneration =
                System.currentTimeMillis() / 1000L;

        String token =
                jwtService.generateVerificationJWT(user);

        long afterGeneration =
                System.currentTimeMillis() / 1000L;

        DecodedJWT decodedJWT =
                JWT.decode(token);

        long tokenExpirySeconds =
                decodedJWT
                        .getExpiresAt()
                        .getTime()
                        / 1000L;

        long configuredExpirySeconds =
                jwtProperties
                        .verificationExpiryInSeconds();

        Assertions.assertTrue(
                tokenExpirySeconds
                        >= beforeGeneration
                        + configuredExpirySeconds
        );

        Assertions.assertTrue(
                tokenExpirySeconds
                        <= afterGeneration
                        + configuredExpirySeconds
        );
    }

    @Test
    public void passwordResetTokenUsesConfiguredExpiryTime() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        long beforeGeneration =
                System.currentTimeMillis() / 1000L;

        String token =
                jwtService.generatePasswordResetJWT(user);

        long afterGeneration =
                System.currentTimeMillis() / 1000L;

        DecodedJWT decodedJWT =
                JWT.decode(token);

        long tokenExpirySeconds =
                decodedJWT
                        .getExpiresAt()
                        .getTime()
                        / 1000L;

        long configuredExpirySeconds =
                jwtProperties
                        .passwordResetExpiryInSeconds();

        Assertions.assertTrue(
                tokenExpirySeconds
                        >= beforeGeneration
                        + configuredExpirySeconds
        );

        Assertions.assertTrue(
                tokenExpirySeconds
                        <= afterGeneration
                        + configuredExpirySeconds
        );
    }

    @Test
    public void generatedTokensContainRequiredClaimsAndUniqueJwtIds() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        List<String> generatedTokens =
                List.of(
                        jwtService.generateToken(user),
                        generateSessionRefreshToken(user),
                        jwtService.generateVerificationJWT(user),
                        jwtService.generatePasswordResetJWT(user)
                );

        List<DecodedJWT> decodedTokens =
                generatedTokens
                        .stream()
                        .map(JWT::decode)
                        .toList();

        decodedTokens.forEach(token -> {

            Assertions.assertNotNull(
                    token.getIssuedAt(),
                    "Every JWT must contain an issued-at value."
            );

            Assertions.assertNotNull(
                    token.getId(),
                    "Every JWT must contain a JWT ID."
            );

            Assertions.assertFalse(
                    token.getId().isBlank(),
                    "JWT ID must not be blank."
            );

            Assertions.assertEquals(
                    List.of(
                            jwtProperties.audience()
                    ),
                    token.getAudience(),
                    "Every JWT must target the configured audience."
            );
        });

        long uniqueJwtIds =
                decodedTokens
                        .stream()
                        .map(DecodedJWT::getId)
                        .distinct()
                        .count();

        Assertions.assertEquals(
                generatedTokens.size(),
                uniqueJwtIds,
                "Every generated JWT must have a unique ID."
        );
    }

    private String generateSessionRefreshToken(
            LocalUser user
    ) {

        return jwtService.generateRefreshToken(
                user,
                UUID.randomUUID().toString(),
                0L
        );
    }
}