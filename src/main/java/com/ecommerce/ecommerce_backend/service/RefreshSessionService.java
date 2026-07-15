package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.config.JwtProperties;
import com.ecommerce.ecommerce_backend.dao.RefreshSessionDao;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.RefreshSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {

    private final RefreshSessionDao refreshSessionDao;
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
}