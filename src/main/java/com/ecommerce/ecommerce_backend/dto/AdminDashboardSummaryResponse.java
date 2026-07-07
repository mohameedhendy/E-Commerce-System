package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;

    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long cancelledOrders;

    private long lowStockProducts;
}