package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.AddressRequest;
import com.ecommerce.ecommerce_backend.dto.AddressResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AddressService addressService;

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable Long userId,
                                                              @AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId, user));
    }

    @PostMapping("/{userId}/address")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable Long userId,
                                                      @AuthenticationPrincipal LocalUser user,
                                                      @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.addAddress(userId, user, request));
    }

    @PutMapping("/{userId}/address/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal LocalUser user,
                                                         @PathVariable Long userId,
                                                         @PathVariable Long addressId,
                                                         @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(userId, addressId, user, request));
    }
}