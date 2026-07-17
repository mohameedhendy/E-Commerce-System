package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AdminProductCreateRequest;
import com.ecommerce.ecommerce_backend.dto.AdminProductRequest;
import com.ecommerce.ecommerce_backend.dto.AdminProductStockRequest;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.Stock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceStockIsolationTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductService productService;

    @Test
    public void updatingProductDetailsDoesNotChangeStock() {

        Product product =
                createProductWithStock(8);

        AdminProductRequest request =
                createDetailsRequest();

        when(productDao.findById(1L))
                .thenReturn(
                        Optional.of(product)
                );

        when(productDao.save(product))
                .thenReturn(product);

        ProductResponse response =
                productService.updateProduct(
                        1L,
                        request
                );

        Assertions.assertEquals(
                8,
                product.getStock().getQuantity(),
                "Updating product details must not change stock."
        );

        Assertions.assertEquals(
                8,
                response.getStockQuantity(),
                "The response should retain the current stock."
        );

        Assertions.assertEquals(
                "Updated Product",
                product.getName()
        );

        Assertions.assertEquals(
                new BigDecimal("149.99"),
                product.getPrice()
        );

        verify(productDao).save(product);
    }

    @Test
    public void creatingProductSetsInitialStock() {

        AdminProductCreateRequest request =
                new AdminProductCreateRequest();

        request.setName("New Product");
        request.setShortDescription("Short description");
        request.setLongDescription("Long description");

        request.setPrice(
                new BigDecimal("99.99")
        );

        request.setStockQuantity(12);

        when(productDao.save(any(Product.class)))
                .thenAnswer(invocation -> {

                    Product product =
                            invocation.getArgument(0);

                    product.setId(2L);

                    return product;
                });

        ProductResponse response =
                productService.createProduct(request);

        Assertions.assertEquals(
                12,
                response.getStockQuantity(),
                "Creation should set the initial stock."
        );
    }

    @Test
    public void dedicatedStockOperationChangesQuantity() {

        Product product =
                createProductWithStock(8);

        AdminProductStockRequest request =
                new AdminProductStockRequest();

        request.setStockQuantity(25);

        when(productDao.findById(1L))
                .thenReturn(
                        Optional.of(product)
                );

        when(productDao.save(product))
                .thenReturn(product);

        ProductResponse response =
                productService.updateProductStock(
                        1L,
                        request
                );

        Assertions.assertEquals(
                25,
                product.getStock().getQuantity()
        );

        Assertions.assertEquals(
                25,
                response.getStockQuantity()
        );

        verify(productDao).save(product);
    }

    private Product createProductWithStock(
            int quantity
    ) {

        Product product = new Product();

        product.setId(1L);
        product.setName("Original Product");
        product.setShortDescription("Original short description");
        product.setLongDescription("Original long description");

        product.setPrice(
                new BigDecimal("100.00")
        );

        product.setActive(true);

        Stock stock = new Stock();

        stock.setId(1L);
        stock.setQuantity(quantity);
        stock.setProduct(product);

        product.setStock(stock);

        return product;
    }

    private AdminProductRequest createDetailsRequest() {

        AdminProductRequest request =
                new AdminProductRequest();

        request.setName("Updated Product");
        request.setShortDescription("Updated short description");
        request.setLongDescription("Updated long description");

        request.setPrice(
                new BigDecimal("149.99")
        );

        return request;
    }
}
