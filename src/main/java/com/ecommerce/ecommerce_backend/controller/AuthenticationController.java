package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.*;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.exception.EmailNotFoundException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistException;
import com.ecommerce.ecommerce_backend.exception.UserNotVerifiedException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.ecommerce_backend.exception.InvalidCredentialsException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody RegistrationBody registrationBody)
            throws UserAlreadyExistException, EmailFailureException {
        userService.registerUser(registrationBody);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody)
            throws UserNotVerifiedException, EmailFailureException, InvalidCredentialsException {

        String jwt = userService.loginUser(loginBody);

        if (jwt == null) {
            throw new InvalidCredentialsException();
        }

        LoginResponse response = new LoginResponse();
        response.setJwt(jwt);
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity verifyEmail(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getLoggedInUser(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/forgot")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email)
            throws EmailNotFoundException, EmailFailureException {
        userService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity resetPassword(@Valid @RequestBody PasswordResetBody body) {
        userService.resetPassword(body);
        return ResponseEntity.ok().build();
    }
}
