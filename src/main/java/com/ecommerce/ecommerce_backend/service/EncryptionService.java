package com.ecommerce.ecommerce_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final PasswordEncoder passwordEncoder;

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean verifyPassword(
            String password,
            String encryptedPassword) {

        return passwordEncoder.matches(
                password,
                encryptedPassword
        );
    }
}