package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getUserOrders(@AuthenticationPrincipal LocalUser user) {
        return orderService.getAllUserOrders(user)
                .stream()
                .map(OrderResponse::new)
                .toList();
    }
}