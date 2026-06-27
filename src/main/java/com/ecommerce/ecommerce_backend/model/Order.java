package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.*;

import java.util.Set;

//@Setter
//@Getter
@Entity
@Table(name = "web_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private LocalUser user;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<ProductOrderQuantity> quantities;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalUser getUser() {
        return user;
    }

    public void setUser(LocalUser user) {
        this.user = user;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Set<ProductOrderQuantity> getQuantities() {
        return quantities;
    }

    public void setQuantities(Set<ProductOrderQuantity> quantities) {
        this.quantities = quantities;
    }
}
