package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetBody {

    @NotBlank(message = "Reset token is required")
    private String token;

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