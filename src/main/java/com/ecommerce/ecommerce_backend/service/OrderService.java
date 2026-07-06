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
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import java.util.HashSet;
import java.util.List;
import com.ecommerce.ecommerce_backend.model.Stock;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.ecommerce_backend.dto.AdminOrderStatusRequest;
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import com.ecommerce.ecommerce_backend.model.Stock;

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

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllUserOrders(LocalUser user, String status, Pageable pageable) {
        Page<Order> orders;

        if (status == null || status.isBlank()) {
            orders = orderDao.findAllByUser(user, pageable);
        } else {
            OrderStatus orderStatus;

            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new InvalidOrderStatusException("Order status must be one of: PENDING, CONFIRMED, CANCELLED");
            }

            orders = orderDao.findAllByUserAndStatus(user, orderStatus, pageable);
        }

        return orders.map(OrderResponse::new);
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

    @Transactional
    public OrderResponse cancelOrder(LocalUser user, Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You are not allowed to cancel this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Order is already cancelled");
        }

        if (order.getStatus() != null && order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Only pending orders can be cancelled");
        }

        order.getQuantities().forEach(item -> {
            Stock stock = item.getProduct().getStock();

            if (stock != null) {
                stock.setQuantity(stock.getQuantity() + item.getQuantity());
            }
        });

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderDao.save(order);

        return new OrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(LocalUser user, Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You are not allowed to access this order");
        }

        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, AdminOrderStatusRequest request) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found"));

        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Cancelled order cannot be updated");
        }

        if (order.getStatus() == newStatus) {
            throw new InvalidOrderStatusException("Order already has this status");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            order.getQuantities().forEach(item -> {
                Stock stock = item.getProduct().getStock();

                if (stock != null) {
                    stock.setQuantity(stock.getQuantity() + item.getQuantity());
                }
            });
        }

        order.setStatus(newStatus);

        Order savedOrder = orderDao.save(order);

        return new OrderResponse(savedOrder);
    }
}