package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.RefreshSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RefreshSessionDao
        extends JpaRepository<RefreshSession, Long> {

    Optional<RefreshSession> findBySessionId(
            String sessionId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT refreshSession
            FROM RefreshSession refreshSession
            JOIN FETCH refreshSession.user
            WHERE refreshSession.sessionId = :sessionId
            """)
    Optional<RefreshSession> findBySessionIdForUpdate(
            @Param("sessionId") String sessionId
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE RefreshSession refreshSession
        SET refreshSession.revoked = true
        WHERE refreshSession.user.id = :userId
          AND refreshSession.revoked = false
        """)
    int revokeAllActiveByUserId(
            @Param("userId") Long userId
    );
}