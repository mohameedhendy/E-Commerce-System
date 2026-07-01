package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.exception.ForbiddenActionException;
import com.ecommerce.ecommerce_backend.exception.InsufficientStockException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class OrderService {

    private final OrderDao orderDao;
    private final AddressDAO addressDAO;
    private final ProductDao productDao;

    public OrderService(OrderDao orderDao, AddressDAO addressDAO, ProductDao productDao) {
        this.orderDao = orderDao;
        this.addressDAO = addressDAO;
        this.productDao = productDao;
    }

    public List<OrderResponse> getAllUserOrders(LocalUser user) {
        return orderDao.findAllByUser(user)
                .stream()
                .map(OrderResponse::new)
                .toList();
    }

    @Transactional
    public OrderResponse createOrder(LocalUser user, OrderRequest request) {
        Address address = addressDAO.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address was not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You are not allowed to use this address");
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setQuantities(new HashSet<>());

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productDao.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));

            Stock stock = product.getStock();

            if (stock == null) {
                throw new InsufficientStockException("Product " + product.getName() + " is out of stock");
            }

            if (stock.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for product " + product.getName()
                );
            }

            stock.setQuantity(stock.getQuantity() - itemRequest.getQuantity());

            ProductOrderQuantity orderQuantity = new ProductOrderQuantity();
            orderQuantity.setOrder(order);
            orderQuantity.setProduct(product);
            orderQuantity.setQuantity(itemRequest.getQuantity());

            order.getQuantities().add(orderQuantity);
        }

        Order savedOrder = orderDao.save(order);

        return new OrderResponse(savedOrder);
    }
}