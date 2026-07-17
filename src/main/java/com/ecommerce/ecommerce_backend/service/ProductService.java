package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AdminProductCreateRequest;
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

    private static final String PRODUCT_NOT_FOUND =
            "Product was not found";

    private final ProductDao productDao;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            String keyword,
            Pageable pageable
    ) {

        Page<Product> products;

        if (hasKeyword(keyword)) {
            products = productDao.searchProducts(
                    keyword.trim(),
                    pageable
            );
        } else {
            products = productDao.findAllByActiveTrue(
                    pageable
            );
        }

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(
            Long productId
    ) {

        Product product =
                getActiveProductOrThrow(productId);

        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(
            AdminProductCreateRequest request
    ) {

        Product product = new Product();

        applyProductDetails(
                product,
                request
        );

        Stock stock =
                getOrCreateStock(product);

        stock.setQuantity(
                request.getStockQuantity()
        );

        product.setActive(true);

        return saveAndConvert(product);
    }

    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            AdminProductRequest request
    ) {

        Product product =
                getProductOrThrow(productId);

        applyProductDetails(
                product,
                request
        );

        return saveAndConvert(product);
    }

    @Transactional
    public void deleteProduct(
            Long productId
    ) {

        Product product =
                getActiveProductOrThrow(productId);

        product.setActive(false);

        productDao.save(product);
    }

    @Transactional
    public ProductResponse restoreProduct(
            Long productId
    ) {

        Product product =
                getProductOrThrow(productId);

        if (Boolean.TRUE.equals(
                product.getActive()
        )) {
            throw new InvalidProductStatusException(
                    "Product is already active"
            );
        }

        product.setActive(true);

        return saveAndConvert(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProductsForAdmin(
            Boolean active,
            Pageable pageable
    ) {

        Page<Product> products;

        if (active == null) {
            products = productDao.findAll(pageable);
        } else {
            products = productDao.findAllByActive(
                    active,
                    pageable
            );
        }

        return toProductResponses(products);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByIdForAdmin(
            Long productId
    ) {

        Product product =
                getProductOrThrow(productId);

        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProductStock(
            Long productId,
            AdminProductStockRequest request
    ) {

        Product product =
                getProductOrThrow(productId);

        Stock stock =
                getOrCreateStock(product);

        stock.setQuantity(
                request.getStockQuantity()
        );

        return saveAndConvert(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getLowStockProductsForAdmin(
            Integer threshold,
            Pageable pageable
    ) {

        Page<Product> products =
                productDao.findLowStockProducts(
                        threshold,
                        pageable
                );

        return toProductResponses(products);
    }

    private Product getProductOrThrow(
            Long productId
    ) {

        return productDao.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PRODUCT_NOT_FOUND
                        )
                );
    }

    private Product getActiveProductOrThrow(
            Long productId
    ) {

        Product product =
                getProductOrThrow(productId);

        if (!Boolean.TRUE.equals(
                product.getActive()
        )) {
            throw new ResourceNotFoundException(
                    PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private void applyProductDetails(
            Product product,
            AdminProductRequest request
    ) {

        product.setName(
                request.getName()
        );

        product.setShortDescription(
                request.getShortDescription()
        );

        product.setLongDescription(
                request.getLongDescription()
        );

        product.setPrice(
                request.getPrice()
        );
    }

    private Stock getOrCreateStock(
            Product product
    ) {

        Stock stock =
                product.getStock();

        if (stock == null) {
            stock = new Stock();
            stock.setProduct(product);
            product.setStock(stock);
        }

        return stock;
    }

    private ProductResponse saveAndConvert(
            Product product
    ) {

        Product savedProduct =
                productDao.save(product);

        return new ProductResponse(
                savedProduct
        );
    }

    private Page<ProductResponse> toProductResponses(
            Page<Product> products
    ) {

        return products.map(
                ProductResponse::new
        );
    }

    private boolean hasKeyword(
            String keyword
    ) {

        return keyword != null
                && !keyword.isBlank();
    }
}
