package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenDAO
        extends JpaRepository<VerificationToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<VerificationToken> findByToken(
            String token
    );

    Optional<VerificationToken>
    findFirstByUser_IdOrderByIdDesc(
            Long userId
    );

    List<VerificationToken>
    findByUser_IdOrderByIdDesc(
            Long userId
    );

    void deleteByUser(LocalUser user);
}