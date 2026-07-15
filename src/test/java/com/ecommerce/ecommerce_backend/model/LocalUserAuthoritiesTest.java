package com.ecommerce.ecommerce_backend.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class LocalUserAuthoritiesTest {

    @Test
    public void userRoleProducesUserAuthority() {

        LocalUser user = new LocalUser();
        user.setRole(Role.USER);

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        Assertions.assertEquals(
                1,
                authorities.size()
        );

        Assertions.assertTrue(
                authorities.stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_USER")
                        )
        );
    }

    @Test
    public void adminRoleProducesAdminAuthority() {

        LocalUser user = new LocalUser();
        user.setRole(Role.ADMIN);

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        Assertions.assertEquals(
                1,
                authorities.size()
        );

        Assertions.assertTrue(
                authorities.stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        )
        );
    }
}