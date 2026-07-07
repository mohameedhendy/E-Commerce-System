package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.OrderDao;
import com.ecommerce.ecommerce_backend.dao.ProductDao;
import com.ecommerce.ecommerce_backend.dto.AdminDashboardSummaryResponse;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final ProductDao productDao;
    private final OrderDao orderDao;

    public AdminDashboardService(ProductDao productDao, OrderDao orderDao) {
        this.productDao = productDao;
        this.orderDao = orderDao;
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        return new AdminDashboardSummaryResponse(
                productDao.count(),
                productDao.countByActive(true),
                productDao.countByActive(false),

                orderDao.count(),
                orderDao.countByStatus(OrderStatus.PENDING),
                orderDao.countByStatus(OrderStatus.CONFIRMED),
                orderDao.countByStatus(OrderStatus.CANCELLED),

                productDao.countLowStockProducts(5)
        );
    }
}