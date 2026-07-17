package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Address;
import lombok.Getter;

@Getter
public class AddressResponse {

    private final Long id;
    private final String addressLine1;
    private final String addressLine2;
    private final String country;
    private final String city;

    public AddressResponse(Address address) {
        this.id = address.getId();
        this.addressLine1 = address.getAddressLine1();
        this.addressLine2 = address.getAddressLine2();
        this.country = address.getCountry();
        this.city = address.getCity();
    }

    public AddressResponse(
            Long id,
            String addressLine1,
            String addressLine2,
            String country,
            String city) {

        this.id = id;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.country = country;
        this.city = city;
    }

}