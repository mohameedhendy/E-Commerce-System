package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.config.AuthRateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class AuthRateLimitService {

    private static final long CLEANUP_INTERVAL =
            1024L;

    private final AuthRateLimitProperties properties;

    private final ConcurrentHashMap<String, RequestWindow>
            requestWindows =
            new ConcurrentHashMap<>();

    private final AtomicLong requestCounter =
            new AtomicLong();

    public RateLimitDecision tryAcquire(
            String clientKey
    ) {

        if (!properties.isEnabled()) {
            return new RateLimitDecision(
                    true,
                    0L
            );
        }

        long currentTime =
                System.currentTimeMillis();

        long windowDurationMillis =
                properties.getWindowSeconds()
                        * 1000L;

        AtomicReference<RateLimitDecision> decision =
                new AtomicReference<>();

        requestWindows.compute(
                clientKey,
                (key, currentWindow) -> {

                    if (currentWindow == null
                            || currentWindow.resetAtMillis()
                            <= currentTime) {

                        decision.set(
                                new RateLimitDecision(
                                        true,
                                        0L
                                )
                        );

                        return new RequestWindow(
                                1,
                                currentTime
                                        + windowDurationMillis
                        );
                    }

                    if (currentWindow.requestCount()
                            >= properties.getMaxRequests()) {

                        long remainingMillis =
                                currentWindow
                                        .resetAtMillis()
                                        - currentTime;

                        long retryAfterSeconds =
                                Math.max(
                                        1L,
                                        (
                                                remainingMillis
                                                        + 999L
                                        ) / 1000L
                                );

                        decision.set(
                                new RateLimitDecision(
                                        false,
                                        retryAfterSeconds
                                )
                        );

                        return currentWindow;
                    }

                    decision.set(
                            new RateLimitDecision(
                                    true,
                                    0L
                            )
                    );

                    return new RequestWindow(
                            currentWindow.requestCount() + 1,
                            currentWindow.resetAtMillis()
                    );
                }
        );

        cleanupExpiredWindows(
                currentTime
        );

        return decision.get();
    }

    private void cleanupExpiredWindows(
            long currentTime
    ) {

        long currentRequestNumber =
                requestCounter.incrementAndGet();

        if (currentRequestNumber
                % CLEANUP_INTERVAL != 0) {

            return;
        }

        requestWindows
                .entrySet()
                .removeIf(entry ->
                        entry.getValue()
                                .resetAtMillis()
                                <= currentTime
                );
    }

    private record RequestWindow(
            int requestCount,
            long resetAtMillis
    ) {
    }

    public record RateLimitDecision(
            boolean allowed,
            long retryAfterSeconds
    ) {
    }
}