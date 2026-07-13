package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AdminProductRequest;
import com.ecommerce.ecommerce_backend.dto.AdminProductStockRequest;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.exception.InvalidProductStatusException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDao productDao;

    public Page<ProductResponse> getAllProducts(String keyword, Pageable pageable) {
        Page<Product> products;

        if (keyword == null || keyword.isBlank()) {
            products = productDao.findAllByActiveTrue(pageable);
        } else {
            products = productDao.searchProducts(keyword.trim(), pageable);
        }

        return products.map(ProductResponse::new);
    }

    public ProductResponse getProductById(Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException("Product was not found");
        }

        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(AdminProductRequest request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setShortDescription(request.getShortDescription());
        product.setLongDescription(request.getLongDescription());
        product.setPrice(request.getPrice());

        Stock stock = new Stock();
        stock.setQuantity(request.getStockQuantity());
        stock.setProduct(product);

        product.setStock(stock);
        product.setActive(true);
        Product savedProduct = productDao.save(product);

        return new ProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, AdminProductRequest request) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        product.setName(request.getName());
        product.setShortDescription(request.getShortDescription());
        product.setLongDescription(request.getLongDescription());
        product.setPrice(request.getPrice());

        Stock stock = product.getStock();

        if (stock == null) {
            stock = new Stock();
            stock.setProduct(product);
            product.setStock(stock);
        }

        stock.setQuantity(request.getStockQuantity());

        Product savedProduct = productDao.save(product);

        return new ProductResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException("Product was not found");
        }

        product.setActive(false);

        productDao.save(product);
    }

    @Transactional
    public ProductResponse restoreProduct(Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        if (Boolean.TRUE.equals(product.getActive())) {
            throw new InvalidProductStatusException("Product is already active");
        }

        product.setActive(true);

        Product savedProduct = productDao.save(product);

        return new ProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProductsForAdmin(Boolean active, Pageable pageable) {
        Page<Product> products;

        if (active == null) {
            products = productDao.findAll(pageable);
        } else {
            products = productDao.findAllByActive(active, pageable);
        }

        return products.map(ProductResponse::new);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByIdForAdmin(Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProductStock(Long productId, AdminProductStockRequest request) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

        Stock stock = product.getStock();

        if (stock == null) {
            stock = new Stock();
            stock.setProduct(product);
            product.setStock(stock);
        }

        stock.setQuantity(request.getStockQuantity());

        Product savedProduct = productDao.save(product);

        return new ProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getLowStockProductsForAdmin(Integer threshold, Pageable pageable) {
        Page<Product> products = productDao.findLowStockProducts(threshold, pageable);

        return products.map(ProductResponse::new);
    }
}