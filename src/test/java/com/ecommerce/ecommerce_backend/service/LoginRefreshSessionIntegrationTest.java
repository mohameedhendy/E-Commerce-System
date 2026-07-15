package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.config.JwtProperties;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.RefreshSessionDao;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.LoginResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.RefreshSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class LoginRefreshSessionIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private RefreshSessionDao refreshSessionDao;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    public void loginCreatesSessionBackedRefreshToken()
            throws Exception {

        LocalUser user = getUserA();

        LoginResponse response =
                loginAsUserA();

        JWTService.RefreshTokenData tokenData =
                jwtService.getRefreshTokenData(
                        response.getRefreshToken()
                );

        Assertions.assertEquals(
                user.getUsername(),
                tokenData.username()
        );

        Assertions.assertEquals(
                user.getRefreshTokenVersion(),
                tokenData.version()
        );

        Assertions.assertNotNull(
                tokenData.sessionId(),
                "Login refresh token must contain a session ID."
        );

        Assertions.assertFalse(
                tokenData.sessionId().isBlank(),
                "Login refresh token session ID must not be blank."
        );

        Assertions.assertEquals(
                0L,
                tokenData.sessionVersion()
        );

        RefreshSession storedSession =
                refreshSessionDao
                        .findBySessionId(
                                tokenData.sessionId()
                        )
                        .orElseThrow();

        Assertions.assertNotNull(
                storedSession.getId()
        );

        Assertions.assertEquals(
                user.getId(),
                storedSession.getUser().getId()
        );

        Assertions.assertEquals(
                tokenData.sessionId(),
                storedSession.getSessionId()
        );

        Assertions.assertEquals(
                tokenData.sessionVersion().longValue(),
                storedSession.getTokenVersion()
        );

        Assertions.assertFalse(
                storedSession.isRevoked()
        );

        Assertions.assertNotNull(
                storedSession.getCreatedAt()
        );

        Assertions.assertNotNull(
                storedSession.getExpiresAt()
        );

        long storedDurationSeconds =
                (
                        storedSession
                                .getExpiresAt()
                                .getTime()
                                - storedSession
                                .getCreatedAt()
                                .getTime()
                ) / 1000L;

        Assertions.assertEquals(
                jwtProperties.refreshExpiryInSeconds(),
                storedDurationSeconds,
                "Stored session duration must match refresh token expiry."
        );
    }

    @Test
    public void separateLoginsCreateIndependentRefreshSessions()
            throws Exception {

        LocalUser user = getUserA();

        LoginResponse firstLogin =
                loginAsUserA();

        LoginResponse secondLogin =
                loginAsUserA();

        JWTService.RefreshTokenData firstTokenData =
                jwtService.getRefreshTokenData(
                        firstLogin.getRefreshToken()
                );

        JWTService.RefreshTokenData secondTokenData =
                jwtService.getRefreshTokenData(
                        secondLogin.getRefreshToken()
                );

        Assertions.assertNotEquals(
                firstTokenData.sessionId(),
                secondTokenData.sessionId(),
                "Every login must create an independent refresh session."
        );

        RefreshSession firstSession =
                refreshSessionDao
                        .findBySessionId(
                                firstTokenData.sessionId()
                        )
                        .orElseThrow();

        RefreshSession secondSession =
                refreshSessionDao
                        .findBySessionId(
                                secondTokenData.sessionId()
                        )
                        .orElseThrow();

        Assertions.assertNotEquals(
                firstSession.getId(),
                secondSession.getId()
        );

        Assertions.assertEquals(
                user.getId(),
                firstSession.getUser().getId()
        );

        Assertions.assertEquals(
                user.getId(),
                secondSession.getUser().getId()
        );

        Assertions.assertFalse(
                firstSession.isRevoked()
        );

        Assertions.assertFalse(
                secondSession.isRevoked()
        );

        Assertions.assertEquals(
                0L,
                firstSession.getTokenVersion()
        );

        Assertions.assertEquals(
                0L,
                secondSession.getTokenVersion()
        );
    }

    private LoginResponse loginAsUserA()
            throws Exception {

        LoginBody loginBody =
                new LoginBody();

        loginBody.setUsername("UserA");
        loginBody.setPassword("PasswordA123");

        return userService.loginUser(
                loginBody
        );
    }

    private LocalUser getUserA() {

        return localUserDao
                .findByUsernameIgnoreCase(
                        "UserA"
                )
                .orElseThrow();
    }
}