package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    private Product product;

    @ManyToOne(optional = false)
    private LocalUser user;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}