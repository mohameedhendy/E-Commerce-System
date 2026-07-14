package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.*;
import com.ecommerce.ecommerce_backend.exception.*;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody RegistrationBody registrationBody)
            throws UserAlreadyExistException, EmailFailureException {

        userService.registerUser(registrationBody);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {

        String token = userService.loginUser(loginBody);

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) throws InvalidTokenException {
        boolean verified = userService.verifyUser(token);

        if (!verified) {
            throw new InvalidTokenException("Invalid or expired verification token");
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getLoggedInUser(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/forgot")
    public ResponseEntity<Void> forgotPassword(
            @RequestParam String email)
            throws EmailFailureException {

        userService.forgotPassword(email);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetBody body)
            throws InvalidTokenException {

        userService.resetPassword(body);
        return ResponseEntity.ok().build();
    }
}