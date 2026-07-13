package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Product;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String shortDescription;
    private String longDescription;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean active;

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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Boolean getActive() {
        return active;
    }
}