package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productDao.findAll(pageable);
        return products.map(ProductResponse::new);
    }
}