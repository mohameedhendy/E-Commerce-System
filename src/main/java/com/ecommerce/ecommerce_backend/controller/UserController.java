package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
import com.ecommerce.ecommerce_backend.dto.AddressRequest;
import com.ecommerce.ecommerce_backend.dto.AddressResponse;
import com.ecommerce.ecommerce_backend.model.Address;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final AddressDAO addressDAO;

    public UserController(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable Long userId,
                                                              @AuthenticationPrincipal LocalUser user) {
        if (!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AddressResponse> addresses = addressDAO.findByUserId(userId)
                .stream()
                .map(AddressResponse::new)
                .toList();

        return ResponseEntity.ok(addresses);
    }

    @Transactional
    @PostMapping("/{userId}/address")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable Long userId,
                                                      @AuthenticationPrincipal LocalUser user,
                                                      @Valid @RequestBody AddressRequest request) {
        if (!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LocalUser refUser = new LocalUser();
        refUser.setId(userId);

        Address address = new Address();
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCountry(request.getCountry());
        address.setCity(request.getCity());
        address.setUser(refUser);

        Address savedAddress = addressDAO.save(address);

        return ResponseEntity.ok(new AddressResponse(savedAddress));
    }

    @Transactional
    @PutMapping("/{userId}/address/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal LocalUser user,
                                                         @PathVariable Long userId,
                                                         @PathVariable Long addressId,
                                                         @Valid @RequestBody AddressRequest request) {
        if (!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Address> optionalAddress = addressDAO.findById(addressId);

        if (optionalAddress.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Address address = optionalAddress.get();

        if (!address.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCountry(request.getCountry());
        address.setCity(request.getCity());

        Address savedAddress = addressDAO.save(address);

        return ResponseEntity.ok(new AddressResponse(savedAddress));
    }

    private boolean userHasPermission(LocalUser user, Long id) {
        return user != null && user.getId().equals(id);
    }
}