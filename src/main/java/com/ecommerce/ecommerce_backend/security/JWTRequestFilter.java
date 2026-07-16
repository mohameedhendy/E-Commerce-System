package com.ecommerce.ecommerce_backend.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTRequestFilter
        extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JWTService jwtService;

    private final DatabaseUserDetailsService
            userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Optional<String> tokenOptional =
                extractBearerToken(request);

        if (tokenOptional.isEmpty()
                || isAlreadyAuthenticated()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {

            authenticateRequest(
                    tokenOptional.get(),
                    request
            );

        } catch (
                JWTVerificationException
                | UsernameNotFoundException
                | IllegalArgumentException exception
        ) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private boolean isAlreadyAuthenticated() {

        return SecurityContextHolder
                .getContext()
                .getAuthentication() != null;
    }

    private Optional<String> extractBearerToken(
            HttpServletRequest request
    ) {

        String authorizationHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                BEARER_PREFIX
        )) {

            return Optional.empty();
        }

        String token =
                authorizationHeader
                        .substring(
                                BEARER_PREFIX.length()
                        )
                        .trim();

        if (token.isBlank()) {

            return Optional.empty();
        }

        return Optional.of(token);
    }

    private void authenticateRequest(
            String token,
            HttpServletRequest request
    ) {

        JWTService.AccessTokenData tokenData =
                jwtService.getAccessTokenData(
                        token
                );

        LocalUser user =
                userDetailsService
                        .loadUserByUsername(
                                tokenData.username()
                        );

        if (!user.isEmailVerified()) {

            return;
        }

        if (tokenData.version() == null
                || user.getRefreshTokenVersion()
                != tokenData
                .version()
                .longValue()) {

            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}