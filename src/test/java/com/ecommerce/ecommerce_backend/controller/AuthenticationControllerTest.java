package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.RefreshSessionDao;
import com.ecommerce.ecommerce_backend.dto.RefreshTokenRequest;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.JWTService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

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
    private JWTService jwtService;
    @Autowired
    private LocalUserDao localUserDao;
    @Autowired
    private RefreshSessionDao refreshSessionDao;
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    public void validRegistrationReturnsOk() throws Exception {

        RegistrationBody body = createValidRegistrationBody();

        performRegistration(body)
                .andExpect(status().isOk());
    }

    @Test
    public void missingRegistrationFieldsReturnBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setUsername(null);

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setUsername("");

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setEmail(null);

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setEmail("");

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setPassword(null);

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setPassword("");

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setFirstName(null);

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setFirstName("");

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setLastName(null);

        performRegistration(body)
                .andExpect(status().isBadRequest());

        body = createValidRegistrationBody();
        body.setLastName("");

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shortUsernameReturnsBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setUsername("ab");

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void longUsernameReturnsBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setUsername("a".repeat(256));

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void invalidEmailReturnsBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setEmail("not-a-valid-email");

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shortPasswordReturnsBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setPassword("Pass123");

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void passwordWithoutNumberReturnsBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setPassword("Password");

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void passwordWithoutLetterReturnsBadRequest()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();
        body.setPassword("12345678");

        performRegistration(body)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void passwordWithSpecialCharactersReturnsOk()
            throws Exception {

        RegistrationBody body = createValidRegistrationBody();

        body.setUsername(
                "AuthenticationSpecialPasswordUser"
        );

        body.setEmail(
                "authentication-special-password@junit.com"
        );

        body.setPassword("Password123!");

        performRegistration(body)
                .andExpect(status().isOk());
    }

    @Test
    public void loginDoesNotApplyRegistrationPasswordPolicy()
            throws Exception {

        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "UserA",
                                          "password": "legacy-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginWithInvalidCredentialsReturnsUnauthorized()
            throws Exception {

        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "UserA",
                                          "password": "WrongPassword123"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginWithBlankPasswordReturnsBadRequest()
            throws Exception {

        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "UserA",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    private ResultActions performRegistration(
            RegistrationBody body
    ) throws Exception {

        return mvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(body)
                        )
        );
    }

    private RegistrationBody createValidRegistrationBody() {

        RegistrationBody body = new RegistrationBody();

        body.setUsername(
                "AuthenticationControllerTestUser"
        );
        body.setEmail(
                "authentication-controller-test@junit.com"
        );
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setPassword("Password123");

        return body;
    }

    @Test
    public void forgotPasswordDoesNotRevealWhetherEmailExists()
            throws Exception {

        mvc.perform(
                        post("/auth/forgot")
                                .param(
                                        "email",
                                        "unknown-user@junit.com"
                                )
                )
                .andExpect(status().isOk());

        Assertions.assertEquals(
                0,
                greenMailExtension
                        .getReceivedMessages()
                        .length,
                "No email should be sent for an unknown address."
        );

        mvc.perform(
                        post("/auth/forgot")
                                .param(
                                        "email",
                                        "UserA@junit.com"
                                )
                )
                .andExpect(status().isOk());

        Assertions.assertEquals(
                1,
                greenMailExtension
                        .getReceivedMessages()
                        .length,
                "A reset email should be sent for an existing account."
        );
    }

    @Test
    public void validLoginReturnsAccessAndRefreshTokens()
            throws Exception {

        mvc.perform(
                        post("/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "UserA",
                                          "password": "PasswordA123"
                                        }
                                        """)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .isNotEmpty()
                );
    }

    @Test
    public void validRefreshTokenReturnsNewTokenPair()
            throws Exception {

        String refreshToken =
                loginAndGetRefreshToken();

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken(refreshToken);

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .isNotEmpty()
                );
    }

    @Test
    public void accessTokenCannotBeUsedForRefresh()
            throws Exception {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken(
                jwtService.generateToken(user)
        );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid or expired refresh token"
                                )
                );
    }

    @Test
    public void blankRefreshTokenReturnsBadRequest()
            throws Exception {

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refreshToken": ""
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.refreshToken"
                        ).value(
                                "Refresh token is required"
                        )
                );
    }

    @Test
    @Transactional
    public void refreshTokenReuseRevokesEntireSession()
            throws Exception {

        String originalRefreshToken =
                loginAndGetRefreshToken();

        RefreshTokenRequest originalRequest =
                new RefreshTokenRequest();

        originalRequest.setRefreshToken(
                originalRefreshToken
        );

        String originalRequestBody =
                objectMapper.writeValueAsString(
                        originalRequest
                );

        String firstRefreshResponse =
                mvc.perform(
                                post("/auth/refresh")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                originalRequestBody
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andExpect(
                                jsonPath("$.accessToken")
                                        .isNotEmpty()
                        )
                        .andExpect(
                                jsonPath("$.refreshToken")
                                        .isNotEmpty()
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String rotatedRefreshToken =
                objectMapper
                        .readTree(
                                firstRefreshResponse
                        )
                        .get("refreshToken")
                        .asText();

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        originalRequestBody
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid or expired refresh token"
                                )
                );

        RefreshTokenRequest rotatedRequest =
                new RefreshTokenRequest();

        rotatedRequest.setRefreshToken(
                rotatedRefreshToken
        );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                rotatedRequest
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid or expired refresh token"
                                )
                );
    }

    @Test
    @Transactional
    public void validLogoutInvalidatesRefreshToken()
            throws Exception {

        String refreshToken =
                loginAndGetRefreshToken();

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken(refreshToken);

        String requestBody =
                objectMapper.writeValueAsString(
                        request
                );

        mvc.perform(
                        post("/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isNoContent()
                );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid or expired refresh token"
                                )
                );
    }

    @Test
    public void accessTokenCannotBeUsedForLogout()
            throws Exception {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken(
                jwtService.generateToken(user)
        );

        mvc.perform(
                        post("/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid or expired refresh token"
                                )
                );
    }

    @Test
    @Transactional
    public void logoutDoesNotInvalidateAnotherLoginSession()
            throws Exception {

        String firstRefreshToken =
                loginAndGetRefreshToken();

        String secondRefreshToken =
                loginAndGetRefreshToken();

        RefreshTokenRequest logoutRequest =
                new RefreshTokenRequest();

        logoutRequest.setRefreshToken(
                firstRefreshToken
        );

        mvc.perform(
                        post("/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                logoutRequest
                                        )
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        RefreshTokenRequest secondSessionRequest =
                new RefreshTokenRequest();

        secondSessionRequest.setRefreshToken(
                secondRefreshToken
        );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                secondSessionRequest
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .isNotEmpty()
                );
    }

    @Test
    @Transactional
    public void logoutAllInvalidatesEveryAccessAndRefreshToken()
            throws Exception {

        JsonNode firstLogin =
                loginAndGetTokenPair();

        JsonNode secondLogin =
                loginAndGetTokenPair();

        String firstAccessToken =
                firstLogin
                        .get("accessToken")
                        .asText();

        String secondAccessToken =
                secondLogin
                        .get("accessToken")
                        .asText();

        mvc.perform(
                        get("/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + firstAccessToken
                                )
                )
                .andExpect(
                        status().isOk()
                );

        mvc.perform(
                        get("/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + secondAccessToken
                                )
                )
                .andExpect(
                        status().isOk()
                );

        mvc.perform(
                        post("/auth/logout-all")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + firstAccessToken
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        mvc.perform(
                        get("/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + firstAccessToken
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        mvc.perform(
                        get("/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + secondAccessToken
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        RefreshTokenRequest firstRequest =
                new RefreshTokenRequest();

        firstRequest.setRefreshToken(
                firstLogin
                        .get("refreshToken")
                        .asText()
        );

        RefreshTokenRequest secondRequest =
                new RefreshTokenRequest();

        secondRequest.setRefreshToken(
                secondLogin
                        .get("refreshToken")
                        .asText()
        );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                firstRequest
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                secondRequest
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    public void logoutAllRequiresAuthentication()
            throws Exception {

        mvc.perform(
                        post("/auth/logout-all")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    private String loginAndGetRefreshToken()
            throws Exception {

        return loginAndGetTokenPair()
                .get("refreshToken")
                .asText();
    }

    private JsonNode loginAndGetTokenPair()
            throws Exception {

        String responseBody =
                mvc.perform(
                                post("/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content("""
                                                {
                                                  "username": "UserA",
                                                  "password": "PasswordA123"
                                                }
                                                """)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(
                responseBody
        );
    }

    @Test
    @Transactional
    public void authenticatedUserCanViewActiveSessions()
            throws Exception {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        int activeSessionsBeforeLogin =
                refreshSessionDao
                        .findAllByUser_IdAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                                user.getId(),
                                Timestamp.from(
                                        Instant.now()
                                )
                        )
                        .size();

        JsonNode firstLogin =
                loginAndGetTokenPair();

        loginAndGetTokenPair();

        mvc.perform(
                        get("/auth/sessions")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + firstLogin
                                                .get("accessToken")
                                                .asText()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(
                                        activeSessionsBeforeLogin + 2
                                )
                )
                .andExpect(
                        jsonPath("$[0].sessionId")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$[0].createdAt")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$[0].expiresAt")
                                .isNotEmpty()
                );
    }

    @Test
    public void viewingSessionsRequiresAuthentication()
            throws Exception {

        mvc.perform(
                        get("/auth/sessions")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    @Transactional
    public void authenticatedUserCanRevokeSelectedSession()
            throws Exception {

        JsonNode firstLogin =
                loginAndGetTokenPair();

        JsonNode secondLogin =
                loginAndGetTokenPair();

        String secondRefreshToken =
                secondLogin
                        .get("refreshToken")
                        .asText();

        String secondSessionId =
                jwtService
                        .getRefreshTokenData(
                                secondRefreshToken
                        )
                        .sessionId();

        mvc.perform(
                        delete(
                                "/auth/sessions/{sessionId}",
                                secondSessionId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + firstLogin
                                                .get("accessToken")
                                                .asText()
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        RefreshTokenRequest revokedSessionRequest =
                new RefreshTokenRequest();

        revokedSessionRequest.setRefreshToken(
                secondRefreshToken
        );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                revokedSessionRequest
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        RefreshTokenRequest remainingSessionRequest =
                new RefreshTokenRequest();

        remainingSessionRequest.setRefreshToken(
                firstLogin
                        .get("refreshToken")
                        .asText()
        );

        mvc.perform(
                        post("/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                remainingSessionRequest
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    public void revokingSessionRequiresAuthentication()
            throws Exception {

        mvc.perform(
                        delete(
                                "/auth/sessions/{sessionId}",
                                "some-session-id"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}