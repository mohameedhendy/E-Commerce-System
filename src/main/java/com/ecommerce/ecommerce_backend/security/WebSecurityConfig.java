package com.ecommerce.ecommerce_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;
    private final CustomAuthenticationEntryPoint
            customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler
            customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Value("${app.security.content-security-policy}")
            String contentSecurityPolicy,
            @Value("${app.security.swagger-content-security-policy}")
            String swaggerContentSecurityPolicy
    ) throws Exception {

        RequestMatcher swaggerUiRequestMatcher =
                request -> {

                    String requestUri =
                            request.getRequestURI();

                    String contextPath =
                            request.getContextPath();

                    String requestPath =
                            contextPath.isEmpty()
                                    ? requestUri
                                    : requestUri.substring(
                                    contextPath.length()
                            );

                    return "/swagger-ui.html"
                            .equals(requestPath)
                            || requestPath.startsWith(
                            "/swagger-ui/"
                    );
                };

        ContentSecurityPolicyHeaderWriter
                apiContentSecurityPolicyWriter =
                new ContentSecurityPolicyHeaderWriter(
                        contentSecurityPolicy
                );

        ContentSecurityPolicyHeaderWriter
                swaggerContentSecurityPolicyWriter =
                new ContentSecurityPolicyHeaderWriter(
                        swaggerContentSecurityPolicy
                );

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
                                .addHeaderWriter(
                                        (request, response) -> {

                                            if (swaggerUiRequestMatcher.matches(
                                                    request
                                            )) {

                                                swaggerContentSecurityPolicyWriter
                                                        .writeHeaders(
                                                                request,
                                                                response
                                                        );

                                                return;
                                            }

                                            apiContentSecurityPolicyWriter
                                                    .writeHeaders(
                                                            request,
                                                            response
                                                    );
                                        }
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
                                        HttpMethod.GET,
                                        "/actuator/health",
                                        "/actuator/health/**"
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
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}