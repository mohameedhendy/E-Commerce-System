package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class JUnitUserDetailsService implements UserDetailsService {

    private final LocalUserDao localUserDao;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return localUserDao
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Test user not found: " + username
                        )
                );
    }
}