package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.config.OpenApiConfig;
import com.ecommerce.ecommerce_backend.dto.AddressRequest;
import com.ecommerce.ecommerce_backend.dto.AddressResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Addresses", description = "Authenticated customer address management")
@RestController
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AddressService addressService;

    @Operation(summary = "List a customer's addresses")
    @GetMapping("/{userId}/address")
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable Long userId,
                                                              @AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId, user));
    }

    @Operation(summary = "Add a customer address")
    @PostMapping("/{userId}/address")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable Long userId,
                                                      @AuthenticationPrincipal LocalUser user,
                                                      @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.addAddress(userId, user, request));
    }

    @Operation(summary = "Update a customer address")
    @PutMapping("/{userId}/address/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal LocalUser user,
                                                         @PathVariable Long userId,
                                                         @PathVariable Long addressId,
                                                         @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(userId, addressId, user, request));
    }
}