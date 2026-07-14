package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationBody {

    @NotBlank(message = "Username is required")
    @Size(
            min = 3,
            max = 255,
            message = "Username must be between 3 and 255 characters"
    )
    private String username;

    @NotBlank(message = "First name is required")
    @Size(
            max = 255,
            message = "First name must not exceed 255 characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(
            max = 255,
            message = "Last name must not exceed 255 characters"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(
            max = 320,
            message = "Email must not exceed 320 characters"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 32,
            message = "Password must be between 8 and 32 characters"
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number"
    )
    private String password;
}