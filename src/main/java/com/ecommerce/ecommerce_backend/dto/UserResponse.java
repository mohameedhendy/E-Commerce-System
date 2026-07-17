package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean emailVerified;

    public UserResponse(LocalUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.emailVerified = user.isEmailVerified();
    }

}