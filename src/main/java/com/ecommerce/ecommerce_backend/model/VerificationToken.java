package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "verification_token")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 2048)
    private String token;

    @Column(name = "created_timestamp", nullable = false)
    private Timestamp createdTimeStamp;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private LocalUser user;

}
