package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
import com.ecommerce.ecommerce_backend.dto.AddressRequest;
import com.ecommerce.ecommerce_backend.dto.AddressResponse;
import com.ecommerce.ecommerce_backend.exception.ForbiddenActionException;
import com.ecommerce.ecommerce_backend.model.Address;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressDAO addressDAO;

    @InjectMocks
    private AddressService addressService;

    @Test
    public void addAddressUsesAuthenticatedUser() {

        LocalUser user = createUser(1L);

        AddressRequest request =
                createAddressRequest();

        when(addressDAO.save(any(Address.class)))
                .thenAnswer(invocation -> {

                    Address address =
                            invocation.getArgument(0);

                    address.setId(10L);

                    return address;
                });

        AddressResponse response =
                addressService.addAddress(
                        user.getId(),
                        user,
                        request
                );

        ArgumentCaptor<Address> addressCaptor =
                ArgumentCaptor.forClass(
                        Address.class
                );

        verify(addressDAO).save(
                addressCaptor.capture()
        );

        Address savedAddress =
                addressCaptor.getValue();

        Assertions.assertSame(
                user,
                savedAddress.getUser()
        );

        Assertions.assertEquals(
                "123 Test Street",
                response.getAddressLine1()
        );

        Assertions.assertEquals(
                "Cairo",
                response.getCity()
        );
    }

    @Test
    public void accessingAnotherUsersAddressesIsForbidden() {

        LocalUser currentUser =
                createUser(1L);

        Assertions.assertThrows(
                ForbiddenActionException.class,
                () -> addressService
                        .getUserAddresses(
                                2L,
                                currentUser
                        )
        );

        verify(
                addressDAO,
                never()
        ).findByUserId(any());
    }

    @Test
    public void updatingAnotherUsersAddressIsForbidden() {

        LocalUser currentUser =
                createUser(1L);

        LocalUser addressOwner =
                createUser(2L);

        Address address =
                new Address();

        address.setId(10L);
        address.setUser(addressOwner);

        when(addressDAO.findById(10L))
                .thenReturn(
                        Optional.of(address)
                );

        Assertions.assertThrows(
                ForbiddenActionException.class,
                () -> addressService
                        .updateAddress(
                                1L,
                                10L,
                                currentUser,
                                createAddressRequest()
                        )
        );

        verify(
                addressDAO,
                never()
        ).save(any(Address.class));
    }

    private LocalUser createUser(
            Long id
    ) {

        LocalUser user = new LocalUser();
        user.setId(id);

        return user;
    }

    private AddressRequest createAddressRequest() {

        AddressRequest request =
                new AddressRequest();

        request.setAddressLine1(
                "123 Test Street"
        );

        request.setAddressLine2(
                "Apartment 10"
        );

        request.setCountry("Egypt");
        request.setCity("Cairo");

        return request;
    }
}