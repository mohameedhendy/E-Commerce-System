package com.ecommerce.ecommerce_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;
    private final CustomAuthenticationEntryPoint
            customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler
            customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .headers(headers ->
                        headers
                                .contentTypeOptions(
                                        withDefaults()
                                )
                                .frameOptions(
                                        frameOptions ->
                                                frameOptions.deny()
                                )
                                .referrerPolicy(
                                        referrerPolicy ->
                                                referrerPolicy.policy(
                                                        ReferrerPolicyHeaderWriter
                                                                .ReferrerPolicy
                                                                .NO_REFERRER
                                                )
                                )
                                .contentSecurityPolicy(
                                        contentSecurityPolicy ->
                                                contentSecurityPolicy
                                                        .policyDirectives(
                                                                "default-src 'none'; "
                                                                        + "frame-ancestors 'none'"
                                                        )
                                )
                                .httpStrictTransportSecurity(
                                        hsts ->
                                                hsts
                                                        .maxAgeInSeconds(
                                                                31536000
                                                        )
                                                        .includeSubDomains(
                                                                false
                                                        )
                                )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        customAuthenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        customAccessDeniedHandler
                                )
                )
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/product",
                                        "/product/**"
                                ).permitAll()
                                .requestMatchers(
                                        "/auth/login",
                                        "/auth/register",
                                        "/auth/refresh",
                                        "/auth/logout",
                                        "/auth/forgot",
                                        "/auth/reset",
                                        "/auth/verify",
                                        "/error"
                                ).permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}