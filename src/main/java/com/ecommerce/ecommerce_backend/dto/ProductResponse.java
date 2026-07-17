package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Product;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductResponse {

    private final Long id;
    private final String name;
    private final String shortDescription;
    private final String longDescription;
    private final BigDecimal price;
    private Integer stockQuantity;
    private final Boolean active;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.shortDescription = product.getShortDescription();
        this.longDescription = product.getLongDescription();
        this.price = product.getPrice();
        this.active = product.getActive();

        if (product.getStock() != null) {
            this.stockQuantity =
                    product.getStock().getQuantity();
        }
    }

}