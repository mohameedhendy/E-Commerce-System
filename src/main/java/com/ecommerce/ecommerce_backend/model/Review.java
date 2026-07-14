package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Review {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    private Product product;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    private LocalUser user;

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}