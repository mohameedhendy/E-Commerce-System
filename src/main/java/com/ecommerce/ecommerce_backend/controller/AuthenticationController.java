package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.LoginResponse;
import com.ecommerce.ecommerce_backend.dto.PasswordResetBody;
import com.ecommerce.ecommerce_backend.dto.RefreshTokenRequest;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.dto.UserResponse;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistException;
import com.ecommerce.ecommerce_backend.exception.UserNotVerifiedException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getLoggedInUser(
            @AuthenticationPrincipal
            LocalUser user
    ) {

        return ResponseEntity.ok(
                new UserResponse(user)
        );
    }

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