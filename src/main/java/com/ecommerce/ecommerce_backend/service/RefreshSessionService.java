package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.config.JwtProperties;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.RefreshSessionDao;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.RefreshSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.ecommerce_backend.dto.RefreshSessionResponse;

import java.util.List;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {

    private static final String INVALID_REFRESH_TOKEN =
            "Invalid or expired refresh token";

    private final RefreshSessionDao refreshSessionDao;
    private final LocalUserDao localUserDao;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshSession createSession(
            LocalUser user
    ) {

        Instant createdAt =
                Instant.now();

        Instant expiresAt =
                createdAt.plusSeconds(
                        jwtProperties
                                .refreshExpiryInSeconds()
                );

        RefreshSession refreshSession =
                new RefreshSession();

        refreshSession.setSessionId(
                UUID.randomUUID().toString()
        );

        refreshSession.setUser(user);
        refreshSession.setTokenVersion(0L);
        refreshSession.setRevoked(false);

        refreshSession.setCreatedAt(
                Timestamp.from(createdAt)
        );

        refreshSession.setExpiresAt(
                Timestamp.from(expiresAt)
        );

        return refreshSessionDao.save(
                refreshSession
        );
    }

    @Transactional
    public RefreshSession rotateSession(
            JWTService.RefreshTokenData tokenData
    ) throws InvalidTokenException {

        RefreshSession refreshSession =
                refreshSessionDao
                        .findBySessionIdForUpdate(
                                tokenData.sessionId()
                        )
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        INVALID_REFRESH_TOKEN
                                )
                        );

        validateSession(
                refreshSession,
                tokenData
        );

        refreshSession.setTokenVersion(
                refreshSession.getTokenVersion() + 1
        );

        return refreshSessionDao.saveAndFlush(
                refreshSession
        );
    }

    @Transactional
    public void revokeSession(
            JWTService.RefreshTokenData tokenData
    ) throws InvalidTokenException {

        RefreshSession refreshSession =
                refreshSessionDao
                        .findBySessionIdForUpdate(
                                tokenData.sessionId()
                        )
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        INVALID_REFRESH_TOKEN
                                )
                        );

        validateSession(
                refreshSession,
                tokenData
        );

        refreshSession.setRevoked(true);

        refreshSessionDao.saveAndFlush(
                refreshSession
        );
    }

    @Transactional
    public void revokeAllSessions(
            Long userId
    ) {

        LocalUser user = localUserDao
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user no longer exists"
                        )
                );

        user.setRefreshTokenVersion(
                user.getRefreshTokenVersion() + 1
        );

        localUserDao.saveAndFlush(user);

        refreshSessionDao.revokeAllActiveByUserId(
                userId
        );
    }

    @Transactional(readOnly = true)
    public List<RefreshSessionResponse> getActiveSessions(
            Long userId
    ) {

        Timestamp currentTime =
                Timestamp.from(
                        Instant.now()
                );

        return refreshSessionDao
                .findAllByUser_IdAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        userId,
                        currentTime
                )
                .stream()
                .map(
                        RefreshSessionResponse::from
                )
                .toList();
    }

    @Transactional
    public void revokeOwnedSession(
            Long userId,
            String sessionId
    ) {

        refreshSessionDao
                .revokeBySessionIdAndUserId(
                        sessionId,
                        userId
                );
    }

    private void validateSession(
            RefreshSession refreshSession,
            JWTService.RefreshTokenData tokenData
    ) throws InvalidTokenException {

        LocalUser user =
                refreshSession.getUser();

        boolean usernameMatches =
                user.getUsername()
                        .equalsIgnoreCase(
                                tokenData.username()
                        );

        boolean globalVersionMatches =
                user.getRefreshTokenVersion()
                        == tokenData
                        .version()
                        .longValue();

        boolean sessionVersionMatches =
                refreshSession.getTokenVersion()
                        == tokenData
                        .sessionVersion()
                        .longValue();

        boolean sessionActive =
                !refreshSession.isRevoked();

        boolean sessionNotExpired =
                refreshSession.getExpiresAt() != null
                        && refreshSession
                        .getExpiresAt()
                        .toInstant()
                        .isAfter(
                                Instant.now()
                        );

        boolean userVerified =
                user.isEmailVerified();

        if (!usernameMatches
                || !globalVersionMatches
                || !sessionVersionMatches
                || !sessionActive
                || !sessionNotExpired
                || !userVerified) {

            throw new InvalidTokenException(
                    INVALID_REFRESH_TOKEN
            );
        }
    }
}