package com.ecommerce.ecommerce_backend.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecommerce.ecommerce_backend.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ecommerce.ecommerce_backend.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_backend.dto.CartCheckoutRequest;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping Cart", description = "Authenticated shopping cart operations and checkout")
@RestController
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get the authenticated customer's cart")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal LocalUser user
    ) {

        CartResponse response =
                cartService.getCart(user);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add an item to the shopping cart")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal LocalUser user,
            @Valid @RequestBody AddCartItemRequest request
    ) {

        CartResponse response =
                cartService.addItem(
                        user,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a shopping cart item quantity")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long itemId,
            @Valid @RequestBody
            UpdateCartItemQuantityRequest request
    ) {

        CartResponse response =
                cartService.updateItemQuantity(
                        user,
                        itemId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove an item from the shopping cart")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long itemId
    ) {

        CartResponse response =
                cartService.removeItem(
                        user,
                        itemId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Clear the shopping cart")
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(
            @AuthenticationPrincipal LocalUser user
    ) {

        CartResponse response =
                cartService.clearCart(user);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Checkout the shopping cart")
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal LocalUser user,
            @Valid @RequestBody
            CartCheckoutRequest request
    ) {

        OrderResponse response =
                cartService.checkout(
                        user,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}