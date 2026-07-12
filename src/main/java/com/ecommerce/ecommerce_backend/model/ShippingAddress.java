package com.ecommerce.ecommerce_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ShippingAddress {

    @Column(
            name = "shipping_address_line_1",
            nullable = false,
            length = 512
    )
    private String addressLine1;

    @Column(
            name = "shipping_address_line_2",
            length = 512
    )
    private String addressLine2;

    @Column(
            name = "shipping_country",
            nullable = false,
            length = 75
    )
    private String country;

    @Column(
            name = "shipping_city",
            nullable = false,
            length = 255
    )
    private String city;

    public ShippingAddress(Address address) {
        this.addressLine1 = address.getAddressLine1();
        this.addressLine2 = address.getAddressLine2();
        this.country = address.getCountry();
        this.city = address.getCity();
    }
}