package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
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

    private AddressDAO addressDAO;

    public UserController(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddresses(@PathVariable Long userId,
                                                      @AuthenticationPrincipal LocalUser user){
        if(!userHasPermission(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressDAO.findByUserId(userId));
    }

    @Transactional
    @PostMapping("/{userId}/address")
    public ResponseEntity<Address> addAddress(@PathVariable Long userId
            , @AuthenticationPrincipal LocalUser user,@Valid @RequestBody Address address){
        if(!userHasPermission(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        return ResponseEntity.ok(addressDAO.save(address));
    }

    @Transactional
    @PutMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long userId, @PathVariable Long addressId,
            @Valid @RequestBody Address address){
        if(!userHasPermission(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if(address.getId().equals(addressId)){
            Optional<Address> optionalAddress = addressDAO.findById(addressId);
            if(optionalAddress.isPresent()){
                LocalUser originalUser = optionalAddress.get().getUser();
                if(originalUser.getId().equals(userId)){
                    address.setUser(originalUser);
                    return ResponseEntity.ok(addressDAO.save(address));
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private boolean userHasPermission(LocalUser user, Long id){
        return user.getId().equals(id);
    }
}
