package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.Address;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class OrderShippingAddressSnapshotTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalUserDao localUserDao;

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void orderKeepsOriginalAddressWhenUserAddressChanges() {

        LocalUser user = localUserDao
                .findByUsernameIgnoreCase("UserA")
                .orElseThrow();

        Address address = addressDAO
                .findById(1L)
                .orElseThrow();

        OrderItemRequest itemRequest =
                new OrderItemRequest();

        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);

        OrderRequest orderRequest =
                new OrderRequest();

        orderRequest.setAddressId(address.getId());
        orderRequest.setItems(List.of(itemRequest));

        OrderResponse createdOrder =
                orderService.createOrder(user, orderRequest);

        Assertions.assertEquals(
                "123 Tester Hill",
                createdOrder.getAddress().getAddressLine1()
        );

        Assertions.assertEquals(
                "Testerton",
                createdOrder.getAddress().getCity()
        );

        Assertions.assertEquals(
                "England",
                createdOrder.getAddress().getCountry()
        );

        address.setAddressLine1("999 Updated Street");
        address.setAddressLine2("Updated Apartment");
        address.setCity("Updated City");
        address.setCountry("Updated Country");

        addressDAO.saveAndFlush(address);

        entityManager.clear();

        OrderResponse retrievedOrder =
                orderService.getOrderById(
                        user,
                        createdOrder.getId()
                );

        Assertions.assertEquals(
                "123 Tester Hill",
                retrievedOrder.getAddress().getAddressLine1(),
                "Old order must keep its original address line."
        );

        Assertions.assertNull(
                retrievedOrder.getAddress().getAddressLine2(),
                "Old order must keep its original second address line."
        );

        Assertions.assertEquals(
                "Testerton",
                retrievedOrder.getAddress().getCity(),
                "Old order must keep its original city."
        );

        Assertions.assertEquals(
                "England",
                retrievedOrder.getAddress().getCountry(),
                "Old order must keep its original country."
        );
    }
}