package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Role;
import com.ecommerce.ecommerce_backend.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JwtRoleAuthorizationTest {

    private static final String ADMIN_ENDPOINT =
            "/admin/dashboard/summary";

    private static final String BEARER_PREFIX =
            "Bearer ";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDao localUserDao;

    @Test
    public void userTokenCannotAccessAdminEndpoint()
            throws Exception {

        LocalUser user =
                getVerifiedUser();

        user.setRole(Role.USER);

        localUserDao.saveAndFlush(user);

        String token =
                jwtService.generateToken(user);

        mvc.perform(
                        get(ADMIN_ENDPOINT)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        BEARER_PREFIX + token
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Forbidden")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "You do not have permission to access this resource"
                                )
                );
    }

    @Test
    public void adminTokenCanAccessAdminEndpoint()
            throws Exception {

        LocalUser user =
                getVerifiedUser();

        user.setRole(Role.ADMIN);

        localUserDao.saveAndFlush(user);

        String token =
                jwtService.generateToken(user);

        mvc.perform(
                        get(ADMIN_ENDPOINT)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        BEARER_PREFIX + token
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    private LocalUser getVerifiedUser() {

        LocalUser user =
                localUserDao
                        .findByUsernameIgnoreCase(
                                "UserA"
                        )
                        .orElseThrow();

        if (!user.isEmailVerified()) {
            throw new IllegalStateException(
                    "Security test user must be verified"
            );
        }

        return user;
    }
}