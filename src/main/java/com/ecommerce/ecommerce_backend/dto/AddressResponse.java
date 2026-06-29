package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Address;

public class AddressResponse {

    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String country;
    private String city;

    public AddressResponse(Address address) {
        this.id = address.getId();
        this.addressLine1 = address.getAddressLine1();
        this.addressLine2 = address.getAddressLine2();
        this.country = address.getCountry();
        this.city = address.getCity();
    }

    public Long getId() {
        return id;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }
}