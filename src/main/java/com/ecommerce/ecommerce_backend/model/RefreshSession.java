package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(
        name = "refresh_session",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refresh_session_session_id",
                        columnNames = "session_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_refresh_session_user_id",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
public class RefreshSession {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id")
    private Long id;

    @Column(
            name = "session_id",
            nullable = false,
            length = 36
    )
    private String sessionId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_refresh_session_user"
            )
    )
    private LocalUser user;

    @Column(
            name = "token_version",
            nullable = false
    )
    private long tokenVersion;

    @Column(
            name = "revoked",
            nullable = false
    )
    private boolean revoked;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Timestamp createdAt;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Timestamp expiresAt;
}