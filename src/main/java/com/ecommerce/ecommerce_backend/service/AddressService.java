package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
import com.ecommerce.ecommerce_backend.dto.AddressRequest;
import com.ecommerce.ecommerce_backend.dto.AddressResponse;
import com.ecommerce.ecommerce_backend.exception.ForbiddenActionException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.Address;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final String ADDRESS_NOT_FOUND =
            "Address was not found";

    private static final String ADDRESS_ACCESS_FORBIDDEN =
            "You are not allowed to access this user's addresses";

    private final AddressDAO addressDAO;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(
            Long userId,
            LocalUser currentUser
    ) {

        validateUserPermission(
                currentUser,
                userId
        );

        return addressDAO
                .findByUserId(userId)
                .stream()
                .map(AddressResponse::new)
                .toList();
    }

    @Transactional
    public AddressResponse addAddress(
            Long userId,
            LocalUser currentUser,
            AddressRequest request
    ) {

        validateUserPermission(
                currentUser,
                userId
        );

        Address address = new Address();

        address.setUser(currentUser);

        applyAddressDetails(
                address,
                request
        );

        return saveAndConvert(address);
    }

    @Transactional
    public AddressResponse updateAddress(
            Long userId,
            Long addressId,
            LocalUser currentUser,
            AddressRequest request
    ) {

        validateUserPermission(
                currentUser,
                userId
        );

        Address address =
                getAddressOrThrow(addressId);

        validateAddressOwnership(
                address,
                userId
        );

        applyAddressDetails(
                address,
                request
        );

        return saveAndConvert(address);
    }

    private Address getAddressOrThrow(
            Long addressId
    ) {

        return addressDAO
                .findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ADDRESS_NOT_FOUND
                        )
                );
    }

    private void validateUserPermission(
            LocalUser currentUser,
            Long requestedUserId
    ) {

        if (currentUser == null
                || currentUser.getId() == null
                || !currentUser.getId()
                .equals(requestedUserId)) {

            throw new ForbiddenActionException(
                    ADDRESS_ACCESS_FORBIDDEN
            );
        }
    }

    private void validateAddressOwnership(
            Address address,
            Long userId
    ) {

        if (!address.getUser()
                .getId()
                .equals(userId)) {

            throw new ForbiddenActionException(
                    "You are not allowed to update this address"
            );
        }
    }

    private void applyAddressDetails(
            Address address,
            AddressRequest request
    ) {

        address.setAddressLine1(
                request.getAddressLine1()
        );

        address.setAddressLine2(
                request.getAddressLine2()
        );

        address.setCountry(
                request.getCountry()
        );

        address.setCity(
                request.getCity()
        );
    }

    private AddressResponse saveAndConvert(
            Address address
    ) {

        Address savedAddress =
                addressDAO.save(address);

        return new AddressResponse(
                savedAddress
        );
    }
}