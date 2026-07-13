package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import lombok.Getter;

@Getter
public class UserResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;

    public UserResponse(LocalUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.emailVerified = user.isEmailVerified();
    }

}