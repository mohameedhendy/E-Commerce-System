package com.ecommerce.ecommerce_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "address_line_1",
            nullable = false,
            length = 512
    )
    private String addressLine1;

    @Column(
            name = "address_line_2",
            length = 512
    )
    private String addressLine2;

    @Column(
            name = "country",
            nullable = false,
            length = 75
    )
    private String country;

    @Column(
            name = "city",
            nullable = false
    )
    private String city;

    @JsonIgnore
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private LocalUser user;
}