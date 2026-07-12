package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JWTRequestFilterTest {

    private static final String AUTHENTICATED_PATH =
            "/auth/me";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDao localUserDao;

    @Test
    public void testRequestWithoutAuthorizationHeader()
            throws Exception {

        mvc.perform(get(AUTHENTICATED_PATH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testValidTokenWithoutBearerPrefix()
            throws Exception {

        LocalUser user = getUser("UserA");
        String token = jwtService.generateToken(user);

        mvc.perform(
                        get(AUTHENTICATED_PATH)
                                .header(
                                        "Authorization",
                                        token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testBearerHeaderWithoutToken()
            throws Exception {

        mvc.perform(
                        get(AUTHENTICATED_PATH)
                                .header(
                                        "Authorization",
                                        "Bearer "
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidBearerToken()
            throws Exception {

        mvc.perform(
                        get(AUTHENTICATED_PATH)
                                .header(
                                        "Authorization",
                                        "Bearer InvalidToken"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnverifiedUserCannotAuthenticate()
            throws Exception {

        LocalUser user = getUser("UserB");
        String token = jwtService.generateToken(user);

        mvc.perform(
                        get(AUTHENTICATED_PATH)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testVerifiedUserCanAuthenticate()
            throws Exception {

        LocalUser user = getUser("UserA");
        String token = jwtService.generateToken(user);

        mvc.perform(
                        get(AUTHENTICATED_PATH)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    private LocalUser getUser(String username) {
        return localUserDao
                .findByUsernameIgnoreCase(username)
                .orElseThrow();
    }
}