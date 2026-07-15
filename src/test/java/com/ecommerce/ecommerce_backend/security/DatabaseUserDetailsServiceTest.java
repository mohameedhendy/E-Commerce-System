package com.ecommerce.ecommerce_backend.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class DatabaseUserDetailsServiceTest {

    @Autowired
    private DatabaseUserDetailsService userDetailsService;

    @Test
    public void existingUserCanBeLoadedIgnoringCase() {

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(
                                "usera"
                        );

        Assertions.assertEquals(
                "UserA",
                userDetails.getUsername()
        );

        Assertions.assertTrue(
                userDetails
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_USER")
                        )
        );
    }

    @Test
    public void missingUserThrowsUsernameNotFoundException() {

        UsernameNotFoundException exception =
                Assertions.assertThrows(
                        UsernameNotFoundException.class,
                        () -> userDetailsService
                                .loadUserByUsername(
                                        "missing-security-user"
                                )
                );

        Assertions.assertEquals(
                "User was not found",
                exception.getMessage()
        );
    }
}