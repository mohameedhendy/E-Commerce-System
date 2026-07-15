package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT localUser
            FROM LocalUser localUser
            WHERE localUser.id = :userId
            """)
    Optional<LocalUser> findByIdForUpdate(
            @Param("userId") Long userId
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE LocalUser u
                                    SET u.password = :encodedPassword,
                                        u.passwordResetVersion =
                                            u.passwordResetVersion + 1,
                                        u.refreshTokenVersion =
                                            u.refreshTokenVersion + 1
                                    WHERE u.id = :userId
                                      AND u.passwordResetVersion =
                                            :expectedVersion
            """)
    int updatePasswordIfResetVersionMatches(
            @Param("userId") Long userId,
            @Param("expectedVersion") long expectedVersion,
            @Param("encodedPassword") String encodedPassword
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE LocalUser u
        SET u.refreshTokenVersion =
                u.refreshTokenVersion + 1
        WHERE u.id = :userId
          AND u.refreshTokenVersion =
                :expectedVersion
        """)
    int rotateRefreshTokenVersion(
            @Param("userId") Long userId,
            @Param("expectedVersion") long expectedVersion
    );
}