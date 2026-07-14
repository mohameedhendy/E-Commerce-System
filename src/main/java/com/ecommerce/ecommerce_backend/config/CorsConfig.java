package com.ecommerce.ecommerce_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private static final long PREFLIGHT_CACHE_SECONDS = 3600L;

    private final ApplicationProperties applicationProperties;

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(getFrontendOrigin())
        );

        configuration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT
                )
        );

        /*
         * Authentication uses Bearer JWT headers,
         * not browser cookies.
         */
        configuration.setAllowCredentials(false);

        configuration.setMaxAge(
                PREFLIGHT_CACHE_SECONDS
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    private String getFrontendOrigin() {

        URI frontendUrl =
                applicationProperties
                        .frontend()
                        .url();

        return frontendUrl.getScheme()
                + "://"
                + frontendUrl.getAuthority();
    }
}