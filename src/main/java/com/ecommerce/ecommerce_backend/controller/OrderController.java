package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(orderService.getAllUserOrders(user));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal LocalUser user,
                                                     @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(user, request));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@AuthenticationPrincipal LocalUser user,
                                                     @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(user, orderId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@AuthenticationPrincipal LocalUser user,
                                                      @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(user, orderId));
    }
}