package com.ecommerce.ecommerce_backend.scheduler;

import com.ecommerce.ecommerce_backend.service.RefreshSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RefreshSessionCleanupJobTest {

    @Mock
    private RefreshSessionService refreshSessionService;

    @InjectMocks
    private RefreshSessionCleanupJob refreshSessionCleanupJob;

    @Test
    public void cleanupJobDelegatesToRefreshSessionService() {

        refreshSessionCleanupJob
                .cleanupRefreshSessions();

        verify(refreshSessionService)
                .cleanupExpiredAndRevokedSessions();
    }
}