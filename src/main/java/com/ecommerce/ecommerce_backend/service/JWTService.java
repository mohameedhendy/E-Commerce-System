package com.ecommerce.ecommerce_backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {

    private static final String USERNAME_KEY = "USERNAME";
    private static final String VERIFICATION_EMAIL_KEY = "VERIFICATION_EMAIL";
    private static final String RESET_PASSWORD_EMAIL_KEY = "RESET_PASSWORD_EMAIL";
    private static final String TOKEN_TYPE_KEY = "TOKEN_TYPE";

    private enum TokenType {
        ACCESS,
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiryInSeconds}")
    private Long expiryInSeconds;

    private Algorithm algorithm;

    @PostConstruct
    public void postConstruct() {
        algorithm = Algorithm.HMAC256(algorithmKey);
    }

    public String generateToken(LocalUser user) {
        return JWT.create()
                .withClaim(USERNAME_KEY, user.getUsername())
                .withClaim(TOKEN_TYPE_KEY, TokenType.ACCESS.name())
                .withIssuer(issuer)
                .withExpiresAt(
                        new Date(
                                System.currentTimeMillis()
                                        + (1000 * expiryInSeconds)
                        )
                )
                .sign(algorithm);
    }

    public String generateVerificationJWT(LocalUser user) {
        return JWT.create()
                .withClaim(
                        VERIFICATION_EMAIL_KEY,
                        user.getEmail()
                )
                .withClaim(
                        TOKEN_TYPE_KEY,
                        TokenType.EMAIL_VERIFICATION.name()
                )
                .withIssuer(issuer)
                .withExpiresAt(
                        new Date(
                                System.currentTimeMillis()
                                        + (1000 * expiryInSeconds)
                        )
                )
                .sign(algorithm);
    }

    public String generatePasswordResetJWT(LocalUser user) {
        return JWT.create()
                .withClaim(
                        RESET_PASSWORD_EMAIL_KEY,
                        user.getEmail()
                )
                .withClaim(
                        TOKEN_TYPE_KEY,
                        TokenType.PASSWORD_RESET.name()
                )
                .withIssuer(issuer)
                .withExpiresAt(
                        new Date(
                                System.currentTimeMillis()
                                        + (1000L * 60 * 30)
                        )
                )
                .sign(algorithm);
    }

    public String getUsername(String token) {
        DecodedJWT jwt = verifyToken(
                token,
                TokenType.ACCESS
        );

        return jwt.getClaim(USERNAME_KEY).asString();
    }

    public String getVerificationEmail(String token) {
        DecodedJWT jwt = verifyToken(
                token,
                TokenType.EMAIL_VERIFICATION
        );

        return jwt.getClaim(
                VERIFICATION_EMAIL_KEY
        ).asString();
    }

    public String getResetPasswordEmail(String token) {
        DecodedJWT jwt = verifyToken(
                token,
                TokenType.PASSWORD_RESET
        );

        return jwt.getClaim(
                RESET_PASSWORD_EMAIL_KEY
        ).asString();
    }

    private DecodedJWT verifyToken(
            String token,
            TokenType expectedType) {

        return JWT.require(algorithm)
                .withIssuer(issuer)
                .withClaim(
                        TOKEN_TYPE_KEY,
                        expectedType.name()
                )
                .build()
                .verify(token);
    }
}