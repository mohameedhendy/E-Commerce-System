package com.ecommerce.ecommerce_backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.ecommerce_backend.dao.RefreshSessionDao;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.LoginResponse;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.RefreshSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class RefreshSessionRefreshIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RefreshSessionDao refreshSessionDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Test
    public void refreshRotatesSessionAndReturnsNewTokenPair()
            throws Exception {

        LoginResponse loginResponse =
                loginAsUserA();

        JWTService.RefreshTokenData originalTokenData =
                jwtService.getRefreshTokenData(
                        loginResponse.getRefreshToken()
                );

        Assertions.assertEquals(
                0L,
                originalTokenData.sessionVersion()
        );

        LoginResponse refreshedResponse =
                userService.refreshAccessToken(
                        loginResponse.getRefreshToken()
                );

        JWTService.RefreshTokenData rotatedTokenData =
                jwtService.getRefreshTokenData(
                        refreshedResponse.getRefreshToken()
                );

        Assertions.assertEquals(
                originalTokenData.username(),
                rotatedTokenData.username()
        );

        Assertions.assertEquals(
                originalTokenData.version(),
                rotatedTokenData.version()
        );

        Assertions.assertEquals(
                originalTokenData.sessionId(),
                rotatedTokenData.sessionId(),
                "Refresh rotation must keep the same session ID."
        );

        Assertions.assertEquals(
                originalTokenData.sessionVersion() + 1L,
                rotatedTokenData.sessionVersion(),
                "Refresh rotation must increase the session version."
        );

        RefreshSession storedSession =
                refreshSessionDao
                        .findBySessionId(
                                originalTokenData.sessionId()
                        )
                        .orElseThrow();

        Assertions.assertEquals(
                rotatedTokenData
                        .sessionVersion()
                        .longValue(),
                storedSession.getTokenVersion()
        );

        Assertions.assertFalse(
                storedSession.isRevoked()
        );

        Assertions.assertEquals(
                originalTokenData.username(),
                jwtService.getUsername(
                        refreshedResponse.getAccessToken()
                )
        );

        DecodedJWT decodedRefreshToken =
                JWT.decode(
                        refreshedResponse.getRefreshToken()
                );

        Assertions.assertEquals(
                storedSession
                        .getExpiresAt()
                        .toInstant()
                        .getEpochSecond(),
                decodedRefreshToken
                        .getExpiresAt()
                        .toInstant()
                        .getEpochSecond(),
                "Rotated refresh token must not outlive its stored session."
        );
    }

    @Test
    public void oldRefreshTokenCannotBeReusedAfterRotation()
            throws Exception {

        LoginResponse loginResponse =
                loginAsUserA();

        String originalRefreshToken =
                loginResponse.getRefreshToken();

        LoginResponse firstRefreshResponse =
                userService.refreshAccessToken(
                        originalRefreshToken
                );

        Assertions.assertNotNull(
                firstRefreshResponse.getAccessToken()
        );

        Assertions.assertNotNull(
                firstRefreshResponse.getRefreshToken()
        );

        Assertions.assertThrows(
                InvalidTokenException.class,
                () -> userService.refreshAccessToken(
                        originalRefreshToken
                ),
                "A rotated refresh token must not be reusable."
        );

        LoginResponse secondRefreshResponse =
                userService.refreshAccessToken(
                        firstRefreshResponse
                                .getRefreshToken()
                );

        Assertions.assertNotNull(
                secondRefreshResponse.getAccessToken()
        );

        Assertions.assertNotNull(
                secondRefreshResponse.getRefreshToken()
        );
    }

    @Test
    public void refreshingOneLoginDoesNotInvalidateAnotherLogin()
            throws Exception {

        LoginResponse firstLogin =
                loginAsUserA();

        LoginResponse secondLogin =
                loginAsUserA();

        JWTService.RefreshTokenData firstLoginTokenData =
                jwtService.getRefreshTokenData(
                        firstLogin.getRefreshToken()
                );

        JWTService.RefreshTokenData secondLoginTokenData =
                jwtService.getRefreshTokenData(
                        secondLogin.getRefreshToken()
                );

        Assertions.assertNotEquals(
                firstLoginTokenData.sessionId(),
                secondLoginTokenData.sessionId()
        );

        LoginResponse firstRefreshed =
                userService.refreshAccessToken(
                        firstLogin.getRefreshToken()
                );

        LoginResponse secondRefreshed =
                userService.refreshAccessToken(
                        secondLogin.getRefreshToken()
                );

        JWTService.RefreshTokenData firstRefreshedData =
                jwtService.getRefreshTokenData(
                        firstRefreshed.getRefreshToken()
                );

        JWTService.RefreshTokenData secondRefreshedData =
                jwtService.getRefreshTokenData(
                        secondRefreshed.getRefreshToken()
                );

        Assertions.assertEquals(
                firstLoginTokenData.sessionId(),
                firstRefreshedData.sessionId()
        );

        Assertions.assertEquals(
                secondLoginTokenData.sessionId(),
                secondRefreshedData.sessionId()
        );

        Assertions.assertEquals(
                1L,
                firstRefreshedData.sessionVersion()
        );

        Assertions.assertEquals(
                1L,
                secondRefreshedData.sessionVersion()
        );

        RefreshSession firstStoredSession =
                refreshSessionDao
                        .findBySessionId(
                                firstLoginTokenData.sessionId()
                        )
                        .orElseThrow();

        RefreshSession secondStoredSession =
                refreshSessionDao
                        .findBySessionId(
                                secondLoginTokenData.sessionId()
                        )
                        .orElseThrow();

        Assertions.assertEquals(
                1L,
                firstStoredSession.getTokenVersion()
        );

        Assertions.assertEquals(
                1L,
                secondStoredSession.getTokenVersion()
        );

        Assertions.assertFalse(
                firstStoredSession.isRevoked()
        );

        Assertions.assertFalse(
                secondStoredSession.isRevoked()
        );
    }

    @Test
    public void logoutRevokesOnlyCurrentRefreshSession()
            throws Exception {

        LoginResponse firstLogin =
                loginAsUserA();

        LoginResponse secondLogin =
                loginAsUserA();

        String firstRefreshToken =
                firstLogin.getRefreshToken();

        String secondRefreshToken =
                secondLogin.getRefreshToken();

        JWTService.RefreshTokenData firstTokenData =
                jwtService.getRefreshTokenData(
                        firstRefreshToken
                );

        JWTService.RefreshTokenData secondTokenData =
                jwtService.getRefreshTokenData(
                        secondRefreshToken
                );

        userService.logout(
                firstRefreshToken
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

        Assertions.assertTrue(
                firstSession.isRevoked(),
                "The logged-out session must be revoked."
        );

        Assertions.assertFalse(
                secondSession.isRevoked(),
                "Logging out must not revoke another login session."
        );

        Assertions.assertThrows(
                InvalidTokenException.class,
                () -> userService.refreshAccessToken(
                        firstRefreshToken
                )
        );

        LoginResponse secondSessionResponse =
                userService.refreshAccessToken(
                        secondRefreshToken
                );

        Assertions.assertNotNull(
                secondSessionResponse.getAccessToken()
        );

        Assertions.assertNotNull(
                secondSessionResponse.getRefreshToken()
        );
    }

    @Test
    public void logoutAllRevokesEveryRefreshSession()
            throws Exception {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        long originalGlobalVersion =
                user.getRefreshTokenVersion();

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

        userService.logoutAll(user);

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

        Assertions.assertTrue(
                firstSession.isRevoked()
        );

        Assertions.assertTrue(
                secondSession.isRevoked()
        );

        LocalUser updatedUser = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Assertions.assertEquals(
                originalGlobalVersion + 1,
                updatedUser.getRefreshTokenVersion()
        );

        Assertions.assertThrows(
                InvalidTokenException.class,
                () -> userService.refreshAccessToken(
                        firstLogin.getRefreshToken()
                )
        );

        Assertions.assertThrows(
                InvalidTokenException.class,
                () -> userService.refreshAccessToken(
                        secondLogin.getRefreshToken()
                )
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
}