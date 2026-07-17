package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.RefreshSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
public class RefreshSessionRepositoryTest {

    private static final long SESSION_DURATION_MILLIS =
            30L * 24L * 60L * 60L * 1000L;

    @Autowired
    private RefreshSessionDao refreshSessionDao;

    @Autowired
    private LocalUserDao localUserDao;

    @Test
    public void refreshSessionCanBeSavedAndFoundBySessionId() {

        LocalUser user = getUserA();

        RefreshSession session =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        RefreshSession savedSession =
                refreshSessionDao.saveAndFlush(
                        session
                );

        RefreshSession storedSession =
                refreshSessionDao
                        .findBySessionId(
                                savedSession.getSessionId()
                        )
                        .orElseThrow();

        Assertions.assertNotNull(
                storedSession.getId()
        );

        Assertions.assertEquals(
                savedSession.getSessionId(),
                storedSession.getSessionId()
        );

        Assertions.assertEquals(
                user.getId(),
                storedSession.getUser().getId()
        );

        Assertions.assertEquals(
                0L,
                storedSession.getTokenVersion()
        );

        Assertions.assertFalse(
                storedSession.isRevoked()
        );

        Assertions.assertNotNull(
                storedSession.getCreatedAt()
        );

        Assertions.assertNotNull(
                storedSession.getExpiresAt()
        );
    }

    @Test
    public void refreshSessionCanBeLoadedForUpdate() {

        LocalUser user = getUserA();

        RefreshSession session =
                refreshSessionDao.saveAndFlush(
                        createRefreshSession(
                                user,
                                UUID.randomUUID().toString()
                        )
                );

        RefreshSession lockedSession =
                refreshSessionDao
                        .findBySessionIdForUpdate(
                                session.getSessionId()
                        )
                        .orElseThrow();

        Assertions.assertEquals(
                session.getId(),
                lockedSession.getId()
        );

        Assertions.assertEquals(
                user.getId(),
                lockedSession.getUser().getId()
        );
    }

    @Test
    public void duplicateSessionIdIsRejected() {

        LocalUser user = getUserA();

        String sessionId =
                UUID.randomUUID().toString();

        RefreshSession firstSession =
                createRefreshSession(
                        user,
                        sessionId
                );

        refreshSessionDao.saveAndFlush(
                firstSession
        );

        RefreshSession duplicateSession =
                createRefreshSession(
                        user,
                        sessionId
                );

        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> refreshSessionDao.saveAndFlush(
                        duplicateSession
                ),
                "Refresh session ID must be unique."
        );
    }

    @Test
    public void cleanupDeletesRevokedAndExpiredSessions() {

        LocalUser user = getUserA();

        RefreshSession activeSession =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        RefreshSession revokedSession =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        revokedSession.setRevoked(true);

        RefreshSession expiredSession =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        expiredSession.setExpiresAt(
                Timestamp.from(
                        Instant.now()
                                .minusSeconds(60)
                )
        );

        refreshSessionDao.saveAllAndFlush(
                List.of(
                        activeSession,
                        revokedSession,
                        expiredSession
                )
        );

        int deletedSessions =
                refreshSessionDao
                        .deleteExpiredOrRevokedSessions(
                                Timestamp.from(
                                        Instant.now()
                                )
                        );

        Assertions.assertTrue(
                deletedSessions >= 2
        );

        Assertions.assertTrue(
                refreshSessionDao
                        .findBySessionId(
                                activeSession.getSessionId()
                        )
                        .isPresent()
        );

        Assertions.assertTrue(
                refreshSessionDao
                        .findBySessionId(
                                revokedSession.getSessionId()
                        )
                        .isEmpty()
        );

        Assertions.assertTrue(
                refreshSessionDao
                        .findBySessionId(
                                expiredSession.getSessionId()
                        )
                        .isEmpty()
        );
    }

    private LocalUser getUserA() {

        return localUserDao
                .findByUsernameIgnoreCase(
                        "UserA"
                )
                .orElseThrow();
    }

    private RefreshSession createRefreshSession(
            LocalUser user,
            String sessionId
    ) {

        long currentTimeMillis =
                System.currentTimeMillis();

        RefreshSession session =
                new RefreshSession();

        session.setSessionId(sessionId);
        session.setUser(user);
        session.setTokenVersion(0L);
        session.setRevoked(false);

        session.setCreatedAt(
                new Timestamp(
                        currentTimeMillis
                )
        );

        session.setExpiresAt(
                new Timestamp(
                        currentTimeMillis
                                + SESSION_DURATION_MILLIS
                )
        );

        return session;
    }

    @Test
    public void activeSessionQueryExcludesRevokedAndExpiredSessions() {

        LocalUser user = getUserA();

        RefreshSession activeSession =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        RefreshSession revokedSession =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        revokedSession.setRevoked(true);

        RefreshSession expiredSession =
                createRefreshSession(
                        user,
                        UUID.randomUUID().toString()
                );

        expiredSession.setExpiresAt(
                Timestamp.from(
                        Instant.now()
                                .minusSeconds(60)
                )
        );

        refreshSessionDao.saveAllAndFlush(
                List.of(
                        activeSession,
                        revokedSession,
                        expiredSession
                )
        );

        List<RefreshSession> activeSessions =
                refreshSessionDao
                        .findAllByUser_IdAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                                user.getId(),
                                Timestamp.from(
                                        Instant.now()
                                )
                        );

        Assertions.assertEquals(
                1,
                activeSessions.size()
        );

        Assertions.assertEquals(
                activeSession.getSessionId(),
                activeSessions
                        .getFirst()
                        .getSessionId()
        );
    }
}