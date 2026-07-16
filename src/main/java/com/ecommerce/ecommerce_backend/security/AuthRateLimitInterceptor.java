package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.config.AuthRateLimitProperties;
import com.ecommerce.ecommerce_backend.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthRateLimitInterceptor
        implements HandlerInterceptor {

    private static final String RATE_LIMIT_MESSAGE =
            "Too many requests. Please try again later.";

    private final AuthRateLimitService
            authRateLimitService;

    private final AuthRateLimitProperties
            properties;

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {

        if (!properties.isEnabled()
                || !HttpMethod.POST.matches(
                request.getMethod()
        )) {

            return true;
        }

        String clientAddress =
                request.getRemoteAddr() == null
                        ? "unknown"
                        : request.getRemoteAddr();

        String clientKey =
                clientAddress
                        + "|"
                        + request.getMethod()
                        + "|"
                        + request.getRequestURI();

        AuthRateLimitService.RateLimitDecision
                decision =
                authRateLimitService.tryAcquire(
                        clientKey
                );

        if (decision.allowed()) {
            return true;
        }

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setHeader(
                HttpHeaders.RETRY_AFTER,
                Long.toString(
                        decision.retryAfterSeconds()
                )
        );

        ApiErrorResponse responseBody =
                new ApiErrorResponse(
                        HttpStatus.TOO_MANY_REQUESTS
                                .value(),
                        HttpStatus.TOO_MANY_REQUESTS
                                .getReasonPhrase(),
                        RATE_LIMIT_MESSAGE
                );

        objectMapper.writeValue(
                response.getOutputStream(),
                responseBody
        );

        return false;
    }
}