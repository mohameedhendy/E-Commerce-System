package com.ecommerce.ecommerce_backend.scheduler;

import com.ecommerce.ecommerce_backend.service.RefreshSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshSessionCleanupJob {

    private final RefreshSessionService refreshSessionService;

    @Scheduled(
            cron = "${app.refresh-session.cleanup-cron:0 0 3 * * *}"
    )
    public void cleanupRefreshSessions() {

        refreshSessionService
                .cleanupExpiredAndRevokedSessions();
    }
}