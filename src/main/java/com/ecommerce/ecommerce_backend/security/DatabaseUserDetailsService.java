package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private static final String USER_NOT_FOUND =
            "User was not found";

    private final LocalUserDao localUserDao;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        return localUserDao
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                USER_NOT_FOUND
                        )
                );
    }
}