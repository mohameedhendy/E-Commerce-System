package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocalUserDao
        extends JpaRepository<LocalUser, Long> {

    Optional<LocalUser> findByUsernameIgnoreCase(
            String username
    );

    Optional<LocalUser> findByEmailIgnoreCase(
            String email
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE LocalUser u
            SET u.password = :encodedPassword,
                u.passwordResetVersion =
                    u.passwordResetVersion + 1
            WHERE u.id = :userId
              AND u.passwordResetVersion =
                    :expectedVersion
            """)
    int updatePasswordIfResetVersionMatches(
            @Param("userId") Long userId,
            @Param("expectedVersion") long expectedVersion,
            @Param("encodedPassword") String encodedPassword
    );
}