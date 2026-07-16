package com.ecommerce.ecommerce_backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.ecommerce_backend.config.JwtProperties;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JWTService {

    private static final String USERNAME_KEY =
            "USERNAME";

    private static final String VERIFICATION_EMAIL_KEY =
            "VERIFICATION_EMAIL";

    private static final String RESET_PASSWORD_EMAIL_KEY =
            "RESET_PASSWORD_EMAIL";

    private static final String RESET_PASSWORD_VERSION_KEY =
            "RESET_PASSWORD_VERSION";

    private static final String TOKEN_TYPE_KEY =
            "TOKEN_TYPE";

    private static final String REFRESH_TOKEN_VERSION_KEY =
            "REFRESH_TOKEN_VERSION";

    private static final String REFRESH_SESSION_ID_KEY =
            "REFRESH_SESSION_ID";

    private static final String REFRESH_SESSION_VERSION_KEY =
            "REFRESH_SESSION_VERSION";

    private final JwtProperties jwtProperties;

    private Algorithm algorithm;

    private enum TokenType {
        ACCESS,
        REFRESH,
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @PostConstruct
    void initializeAlgorithm() {

        algorithm = Algorithm.HMAC256(
                jwtProperties
                        .algorithm()
                        .key()
        );
    }

    public String generateToken(
            LocalUser user
    ) {

        return generateUserToken(
                user,
                TokenType.ACCESS,
                jwtProperties.expiryInSeconds()
        );
    }

    public String generateRefreshToken(
            LocalUser user,
            String sessionId,
            long sessionVersion
    ) {

        return generateRefreshToken(
                user,
                sessionId,
                sessionVersion,
                createExpiryDate(
                        jwtProperties
                                .refreshExpiryInSeconds()
                )
        );
    }

    public String generateRefreshToken(
            LocalUser user,
            String sessionId,
            long sessionVersion,
            Date expiresAt
    ) {

        validateRefreshSessionData(
                sessionId,
                sessionVersion,
                expiresAt
        );

        return buildRefreshToken(
                user,
                sessionId,
                sessionVersion,
                expiresAt
        );
    }

    public String generateVerificationJWT(
            LocalUser user
    ) {

        return createTokenBuilder(
                TokenType.EMAIL_VERIFICATION
        )
                .withClaim(
                        VERIFICATION_EMAIL_KEY,
                        user.getEmail()
                )
                .withExpiresAt(
                        createExpiryDate(
                                jwtProperties
                                        .verificationExpiryInSeconds()
                        )
                )
                .sign(algorithm);
    }

    public String generatePasswordResetJWT(
            LocalUser user
    ) {

        return createTokenBuilder(
                TokenType.PASSWORD_RESET
        )
                .withClaim(
                        RESET_PASSWORD_EMAIL_KEY,
                        user.getEmail()
                )
                .withClaim(
                        RESET_PASSWORD_VERSION_KEY,
                        user.getPasswordResetVersion()
                )
                .withExpiresAt(
                        createExpiryDate(
                                jwtProperties
                                        .passwordResetExpiryInSeconds()
                        )
                )
                .sign(algorithm);
    }

    public String getUsername(
            String token
    ) {

        return getAccessTokenData(token)
                .username();
    }

    public AccessTokenData getAccessTokenData(
            String token
    ) {

        DecodedJWT jwt = verifyToken(
                token,
                TokenType.ACCESS
        );

        return new AccessTokenData(
                jwt.getClaim(
                        USERNAME_KEY
                ).asString(),
                jwt.getClaim(
                        REFRESH_TOKEN_VERSION_KEY
                ).asLong()
        );
    }

    public String getRefreshUsername(
            String token
    ) {

        return getRefreshTokenData(token)
                .username();
    }

    public RefreshTokenData getRefreshTokenData(
            String token
    ) {

        DecodedJWT jwt = verifyToken(
                token,
                TokenType.REFRESH
        );

        return new RefreshTokenData(
                jwt.getClaim(
                        USERNAME_KEY
                ).asString(),
                jwt.getClaim(
                        REFRESH_TOKEN_VERSION_KEY
                ).asLong(),
                jwt.getClaim(
                        REFRESH_SESSION_ID_KEY
                ).asString(),
                jwt.getClaim(
                        REFRESH_SESSION_VERSION_KEY
                ).asLong()
        );
    }

    public String getVerificationEmail(
            String token
    ) {

        DecodedJWT jwt = verifyToken(
                token,
                TokenType.EMAIL_VERIFICATION
        );

        return jwt
                .getClaim(
                        VERIFICATION_EMAIL_KEY
                )
                .asString();
    }

    public PasswordResetTokenData getPasswordResetData(
            String token
    ) {

        DecodedJWT jwt = verifyToken(
                token,
                TokenType.PASSWORD_RESET
        );

        return new PasswordResetTokenData(
                jwt.getClaim(
                        RESET_PASSWORD_EMAIL_KEY
                ).asString(),
                jwt.getClaim(
                        RESET_PASSWORD_VERSION_KEY
                ).asLong()
        );
    }

    public String getResetPasswordEmail(
            String token
    ) {

        return getPasswordResetData(token)
                .email();
    }

    private String buildRefreshToken(
            LocalUser user,
            String sessionId,
            long sessionVersion,
            Date expiresAt
    ) {

        return createTokenBuilder(
                TokenType.REFRESH
        )
                .withClaim(
                        USERNAME_KEY,
                        user.getUsername()
                )
                .withClaim(
                        REFRESH_TOKEN_VERSION_KEY,
                        user.getRefreshTokenVersion()
                )
                .withClaim(
                        REFRESH_SESSION_ID_KEY,
                        sessionId
                )
                .withClaim(
                        REFRESH_SESSION_VERSION_KEY,
                        sessionVersion
                )
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    private String generateUserToken(
            LocalUser user,
            TokenType tokenType,
            long expirySeconds
    ) {

        return createTokenBuilder(
                tokenType
        )
                .withClaim(
                        USERNAME_KEY,
                        user.getUsername()
                )
                .withClaim(
                        REFRESH_TOKEN_VERSION_KEY,
                        user.getRefreshTokenVersion()
                )
                .withExpiresAt(
                        createExpiryDate(
                                expirySeconds
                        )
                )
                .sign(algorithm);
    }

    private DecodedJWT verifyToken(
            String token,
            TokenType expectedType
    ) {

        return JWT.require(algorithm)
                .withIssuer(
                        jwtProperties.issuer()
                )
                .withAudience(
                        jwtProperties.audience()
                )
                .withClaim(
                        TOKEN_TYPE_KEY,
                        expectedType.name()
                )
                .build()
                .verify(token);
    }

    private JWTCreator.Builder createTokenBuilder(
            TokenType tokenType
    ) {

        return JWT.create()
                .withClaim(
                        TOKEN_TYPE_KEY,
                        tokenType.name()
                )
                .withIssuer(
                        jwtProperties.issuer()
                )
                .withAudience(
                        jwtProperties.audience()
                )
                .withIssuedAt(
                        new Date()
                )
                .withJWTId(
                        UUID.randomUUID()
                                .toString()
                );
    }

    private void validateRefreshSessionData(
            String sessionId,
            long sessionVersion,
            Date expiresAt
    ) {

        if (sessionId == null
                || sessionId.isBlank()) {

            throw new IllegalArgumentException(
                    "Refresh session ID is required"
            );
        }

        if (sessionVersion < 0) {

            throw new IllegalArgumentException(
                    "Refresh session version cannot be negative"
            );
        }

        if (expiresAt == null
                || !expiresAt.after(new Date())) {

            throw new IllegalArgumentException(
                    "Refresh session expiry must be in the future"
            );
        }
    }

    private Date createExpiryDate(
            long expirySeconds
    ) {

        return new Date(
                System.currentTimeMillis()
                        + expirySeconds * 1000L
        );
    }

    public record AccessTokenData(
            String username,
            Long version
    ) {
    }

    public record RefreshTokenData(
            String username,
            Long version,
            String sessionId,
            Long sessionVersion
    ) {
    }

    public record PasswordResetTokenData(
            String email,
            Long version
    ) {
    }
}