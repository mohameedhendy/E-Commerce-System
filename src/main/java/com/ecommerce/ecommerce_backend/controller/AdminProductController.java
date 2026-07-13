package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.AdminProductRequest;
import com.ecommerce.ecommerce_backend.dto.AdminProductStockRequest;
import com.ecommerce.ecommerce_backend.dto.PagedResponse;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/product")
@Validated
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long productId,
                                                         @Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/restore")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.restoreProduct(productId));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProductsForAdmin(
            @RequestParam(required = false) Boolean active,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size must not exceed 50")
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<ProductResponse> products = productService.getAllProductsForAdmin(active, pageable);

        return ResponseEntity.ok(new PagedResponse<>(products));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<PagedResponse<ProductResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "5")
            @Min(value = 0, message = "Threshold must be 0 or greater")
            int threshold,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be 0 or greater")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size must not exceed 50")
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "stock.quantity"));

        Page<ProductResponse> products = productService.getLowStockProductsForAdmin(threshold, pageable);

        return ResponseEntity.ok(new PagedResponse<>(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductByIdForAdmin(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductByIdForAdmin(productId));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(@PathVariable Long productId,
                                                              @Valid @RequestBody AdminProductStockRequest request) {
        return ResponseEntity.ok(productService.updateProductStock(productId, request));
    }
}