package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.config.OpenApiConfig;
import com.ecommerce.ecommerce_backend.dto.*;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistException;
import com.ecommerce.ecommerce_backend.exception.UserNotVerifiedException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Authentication", description = "Registration, login, token lifecycle and account sessions")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @Operation(summary = "Register a new customer account")
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @Valid
            @RequestBody
            RegistrationBody registrationBody
    ) throws UserAlreadyExistException,
            EmailFailureException {

        userService.registerUser(
                registrationBody
        );

        return ResponseEntity
                .ok()
                .build();
    }

    @Operation(summary = "Authenticate a user")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid
            @RequestBody
            LoginBody loginBody
    ) throws UserNotVerifiedException,
            EmailFailureException {

        LoginResponse response =
                userService.loginUser(
                        loginBody
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Issue new tokens using a refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ) throws InvalidTokenException {

        LoginResponse response =
                userService.refreshAccessToken(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revoke a refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ) throws InvalidTokenException {

        userService.logout(
                request.getRefreshToken()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    @Operation(summary = "Revoke all active sessions")
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal
            LocalUser user
    ) {

        userService.logoutAll(user);

        return ResponseEntity
                .noContent()
                .build();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    @Operation(summary = "List active authentication sessions")
    @GetMapping("/sessions")
    public ResponseEntity<List<RefreshSessionResponse>>
    getActiveSessions(
            @AuthenticationPrincipal
            LocalUser user
    ) {

        return ResponseEntity.ok(
                userService.getActiveSessions(user)
        );
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    @Operation(summary = "Revoke a specific authentication session")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal
            LocalUser user,
            @PathVariable
            String sessionId
    ) {

        userService.revokeSession(
                user,
                sessionId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(summary = "Verify a customer email address")
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(
            @RequestParam
            String token
    ) throws InvalidTokenException {

        boolean verified =
                userService.verifyUser(token);

        if (!verified) {
            throw new InvalidTokenException(
                    "Invalid or expired verification token"
            );
        }

        return ResponseEntity
                .ok()
                .build();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    @Operation(summary = "Get the authenticated customer profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getLoggedInUser(
            @AuthenticationPrincipal
            LocalUser user
    ) {

        return ResponseEntity.ok(
                new UserResponse(user)
        );
    }

    @Operation(summary = "Request a password reset")
    @PostMapping("/forgot")
    public ResponseEntity<Void> forgotPassword(
            @RequestParam
            String email
    ) throws EmailFailureException {

        userService.forgotPassword(email);

        return ResponseEntity
                .ok()
                .build();
    }

    @Operation(summary = "Reset a customer password")
    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid
            @RequestBody
            PasswordResetBody body
    ) throws InvalidTokenException {

        userService.resetPassword(body);

        return ResponseEntity
                .ok()
                .build();
    }
}