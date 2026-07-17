package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "shopping_cart_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shopping_cart_item_cart_product",
                        columnNames = {
                                "cart_id",
                                "product_id"
                        }
                )
        }
)
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "cart_id",
            nullable = false
    )
    private Cart cart;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    @Version
    @Setter(AccessLevel.NONE)
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;
}