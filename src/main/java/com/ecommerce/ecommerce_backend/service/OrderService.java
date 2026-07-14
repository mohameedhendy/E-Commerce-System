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
import com.ecommerce.ecommerce_backend.model.Address;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.ProductOrderQuantity;
import com.ecommerce.ecommerce_backend.model.ShippingAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import com.ecommerce.ecommerce_backend.util.MoneyUtils;
import java.util.HashSet;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_NOT_FOUND =
            "Order was not found";

    private static final String ADDRESS_NOT_FOUND =
            "Address was not found";

    private static final String PRODUCT_NOT_FOUND =
            "Product was not found";

    private final OrderDao orderDao;
    private final AddressDAO addressDAO;
    private final ProductDao productDao;
    private final StockDao stockDao;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllUserOrders(
            LocalUser user,
            String status,
            Pageable pageable
    ) {

        Page<Order> orders;

        if (hasStatusFilter(status)) {
            OrderStatus orderStatus =
                    parseOrderStatus(status);

            orders = orderDao.findAllByUserAndStatus(
                    user,
                    orderStatus,
                    pageable
            );
        } else {
            orders = orderDao.findAllByUser(
                    user,
                    pageable
            );
        }

        return orders.map(OrderResponse::new);
    }

    @Transactional
    public OrderResponse createOrder(
            LocalUser user,
            OrderRequest request
    ) {

        Address address = getOwnedAddress(
                user,
                request.getAddressId()
        );

        Order order = initializeOrder(
                user,
                address
        );

        BigDecimal orderTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest :
                request.getItems()) {

            ProductOrderQuantity orderItem =
                    createOrderItem(
                            order,
                            itemRequest
                    );

            order.getQuantities().add(orderItem);

            orderTotal = orderTotal.add(
                    calculateItemTotal(orderItem)
            );
        }

        order.setTotalAmount(
                MoneyUtils.scale(orderTotal)
        );

        Order savedOrder =
                orderDao.save(order);

        return new OrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(
            LocalUser user,
            Long orderId
    ) {

        Order order =
                getLockedOrderOrThrow(orderId);

        validateOrderOwnership(
                order,
                user,
                "You are not allowed to cancel this order"
        );

        if (order.getStatus()
                == OrderStatus.CANCELLED) {

            throw new InvalidOrderStatusException(
                    "Order is already cancelled"
            );
        }

        if (order.getStatus()
                != OrderStatus.PENDING) {

            throw new InvalidOrderStatusException(
                    "Only pending orders can be cancelled"
            );
        }

        transitionOrderStatus(
                order,
                OrderStatus.CANCELLED
        );

        Order savedOrder =
                orderDao.save(order);

        return new OrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            LocalUser user,
            Long orderId
    ) {

        Order order =
                getOrderOrThrow(orderId);

        validateOrderOwnership(
                order,
                user,
                "You are not allowed to access this order"
        );

        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            AdminOrderStatusRequest request
    ) {

        Order order =
                getLockedOrderOrThrow(orderId);

        OrderStatus newStatus =
                parseOrderStatus(
                        request.getStatus()
                );

        transitionOrderStatus(
                order,
                newStatus
        );

        Order savedOrder =
                orderDao.save(order);

        return new OrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersForAdmin(
            String status,
            Pageable pageable
    ) {

        Page<Order> orders;

        if (hasStatusFilter(status)) {
            OrderStatus orderStatus =
                    parseOrderStatus(status);

            orders = orderDao.findAllByStatus(
                    orderStatus,
                    pageable
            );
        } else {
            orders = orderDao.findAll(pageable);
        }

        return orders.map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(
            Long orderId
    ) {

        Order order =
                getOrderOrThrow(orderId);

        return new OrderResponse(order);
    }

    private Address getOwnedAddress(
            LocalUser user,
            Long addressId
    ) {

        Address address = addressDAO
                .findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ADDRESS_NOT_FOUND
                        )
                );

        if (!address.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ForbiddenActionException(
                    "You are not allowed to use this address"
            );
        }

        return address;
    }

    private Order initializeOrder(
            LocalUser user,
            Address address
    ) {

        Order order = new Order();

        order.setUser(user);
        order.setAddress(address);
        order.setShippingAddress(
                new ShippingAddress(address)
        );
        order.setQuantities(new HashSet<>());

        return order;
    }

    private ProductOrderQuantity createOrderItem(
            Order order,
            OrderItemRequest itemRequest
    ) {

        Product product = getActiveProduct(
                itemRequest.getProductId()
        );

        decreaseStock(
                product,
                itemRequest.getQuantity()
        );

        BigDecimal unitPrice =
                MoneyUtils.scale(
                        product.getPrice()
                );

        ProductOrderQuantity orderItem =
                new ProductOrderQuantity();

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(
                itemRequest.getQuantity()
        );
        orderItem.setUnitPrice(unitPrice);
        orderItem.setProductName(
                product.getName()
        );

        return orderItem;
    }

    private Product getActiveProduct(
            Long productId
    ) {

        Product product = productDao
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PRODUCT_NOT_FOUND
                        )
                );

        if (!Boolean.TRUE.equals(
                product.getActive()
        )) {
            throw new ResourceNotFoundException(
                    PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private void decreaseStock(
            Product product,
            Integer quantity
    ) {

        int updatedRows = stockDao.decreaseStock(
                product.getId(),
                quantity
        );

        if (updatedRows == 0) {
            throw new InsufficientStockException(
                    "Not enough stock for product "
                            + product.getName()
            );
        }
    }

    private BigDecimal calculateItemTotal(
            ProductOrderQuantity orderItem
    ) {

        return MoneyUtils.calculateTotal(
                orderItem.getUnitPrice(),
                orderItem.getQuantity()
        );
    }

    private void restoreStock(Order order) {

        order.getQuantities()
                .forEach(item -> {

                    int updatedRows =
                            stockDao.increaseStock(
                                    item.getProduct()
                                            .getId(),
                                    item.getQuantity()
                            );

                    if (updatedRows == 0) {
                        throw new ResourceNotFoundException(
                                "Stock was not found for product "
                                        + item.getProduct()
                                        .getName()
                        );
                    }
                });
    }

    private void transitionOrderStatus(
            Order order,
            OrderStatus newStatus
    ) {

        OrderStatus currentStatus =
                order.getStatus();

        if (currentStatus == newStatus) {
            throw new InvalidOrderStatusException(
                    "Order already has status "
                            + newStatus.name()
            );
        }

        if (!currentStatus.canTransitionTo(
                newStatus
        )) {
            throw new InvalidOrderStatusException(
                    "Order status cannot change from "
                            + currentStatus.name()
                            + " to "
                            + newStatus.name()
            );
        }

        if (newStatus
                == OrderStatus.CANCELLED) {

            restoreStock(order);
        }

        order.setStatus(newStatus);
    }

    private Order getOrderOrThrow(
            Long orderId
    ) {

        return orderDao
                .findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ORDER_NOT_FOUND
                        )
                );
    }

    private Order getLockedOrderOrThrow(
            Long orderId
    ) {

        return orderDao
                .findLockedById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ORDER_NOT_FOUND
                        )
                );
    }

    private void validateOrderOwnership(
            Order order,
            LocalUser user,
            String errorMessage
    ) {

        if (!order.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ForbiddenActionException(
                    errorMessage
            );
        }
    }

    private boolean hasStatusFilter(
            String status
    ) {

        return status != null
                && !status.isBlank();
    }

    private OrderStatus parseOrderStatus(
            String status
    ) {

        if (!hasStatusFilter(status)) {
            throw new InvalidOrderStatusException(
                    "Order status is required"
            );
        }

        try {
            return OrderStatus.valueOf(
                    status.trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidOrderStatusException(
                    "Order status must be one of: "
                            + "PENDING, CONFIRMED, CANCELLED"
            );
        }
    }
}