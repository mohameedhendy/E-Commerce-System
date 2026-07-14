package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.AddressDAO;
import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dao.StockDao;
import com.ecommerce.ecommerce_backend.dto.AdminOrderStatusRequest;
import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.exception.ForbiddenActionException;
import com.ecommerce.ecommerce_backend.exception.InsufficientStockException;
import com.ecommerce.ecommerce_backend.exception.InvalidOrderStatusException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderDao orderDao;
    private final AddressDAO addressDAO;
    private final ProductDao productDao;
    private final StockDao stockDao;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllUserOrders(LocalUser user, String status, Pageable pageable) {
        Page<Order> orders;

        if (status == null || status.isBlank()) {
            orders = orderDao.findAllByUser(user, pageable);
        } else {
            OrderStatus orderStatus =
                    parseOrderStatus(status);

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
        order.setShippingAddress(new ShippingAddress(address));
        order.setQuantities(new HashSet<>());
        BigDecimal orderTotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productDao.findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product was not found"
                            )
                    );

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ResourceNotFoundException(
                        "Product was not found"
                );
            }

            int updatedRows = stockDao.decreaseStock(
                    product.getId(),
                    itemRequest.getQuantity()
            );

            if (updatedRows == 0) {
                throw new InsufficientStockException(
                        "Not enough stock for product " + product.getName()
                );
            }

            BigDecimal unitPrice =
                    product.getPrice()
                            .setScale(2, RoundingMode.HALF_UP);

            ProductOrderQuantity orderQuantity =
                    new ProductOrderQuantity();

            orderQuantity.setOrder(order);
            orderQuantity.setProduct(product);
            orderQuantity.setQuantity(itemRequest.getQuantity());
            orderQuantity.setUnitPrice(unitPrice);

            BigDecimal itemTotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
            );

            orderTotal = orderTotal.add(itemTotal);

            order.getQuantities().add(orderQuantity);
        }

        order.setTotalAmount(
                orderTotal.setScale(2, RoundingMode.HALF_UP)
        );

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
            throw new InvalidOrderStatusException(
                    "Order is already cancelled"
            );
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    "Only pending orders can be cancelled"
            );
        }

        transitionOrderStatus(
                order,
                OrderStatus.CANCELLED
        );

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
    public OrderResponse updateOrderStatus(
            Long orderId,
            AdminOrderStatusRequest request) {

        Order order = orderDao.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order was not found"
                        )
                );

        OrderStatus newStatus =
                parseOrderStatus(request.getStatus());

        transitionOrderStatus(order, newStatus);

        Order savedOrder = orderDao.save(order);

        return new OrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersForAdmin(String status, Pageable pageable) {
        Page<Order> orders;

        if (status == null || status.isBlank()) {
            orders = orderDao.findAll(pageable);
        } else {
            OrderStatus orderStatus =
                    parseOrderStatus(status);

            orders = orderDao.findAllByStatus(orderStatus, pageable);
        }

        return orders.map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found"));

        return new OrderResponse(order);
    }

    private void restoreStock(Order order) {

        order.getQuantities().forEach(item -> {

            int updatedRows = stockDao.increaseStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );

            if (updatedRows == 0) {
                throw new ResourceNotFoundException(
                        "Stock was not found for product "
                                + item.getProduct().getName()
                );
            }
        });
    }

    private void transitionOrderStatus(
            Order order,
            OrderStatus newStatus) {

        OrderStatus currentStatus =
                order.getStatus();

        if (currentStatus == newStatus) {
            throw new InvalidOrderStatusException(
                    "Order already has status "
                            + newStatus.name()
            );
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusException(
                    "Order status cannot change from "
                            + currentStatus.name()
                            + " to "
                            + newStatus.name()
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
    }

    private OrderStatus parseOrderStatus(
            String status) {

        if (status == null || status.isBlank()) {
            throw new InvalidOrderStatusException(
                    "Order status is required"
            );
        }

        try {
            return OrderStatus.valueOf(
                    status.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidOrderStatusException(
                    "Order status must be one of: "
                            + "PENDING, CONFIRMED, CANCELLED"
            );
        }
    }
}