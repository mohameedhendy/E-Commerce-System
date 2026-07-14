package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Assertions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        /*
         * The password format is valid, but the value is incorrect.
         * Therefore validation passes and authentication returns 401.
         */
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
}