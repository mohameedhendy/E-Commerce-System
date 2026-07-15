package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.RefreshSession;

import java.time.Instant;

public record RefreshSessionResponse(
        String sessionId,
        Instant createdAt,
        Instant expiresAt
) {

    public static RefreshSessionResponse from(
            RefreshSession session
    ) {

        return new RefreshSessionResponse(
                session.getSessionId(),
                session.getCreatedAt().toInstant(),
                session.getExpiresAt().toInstant()
        );
    }
}