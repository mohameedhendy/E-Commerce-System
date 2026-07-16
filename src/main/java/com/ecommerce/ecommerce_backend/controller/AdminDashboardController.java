package com.ecommerce.ecommerce_backend.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecommerce.ecommerce_backend.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ecommerce.ecommerce_backend.dto.AdminDashboardSummaryResponse;
import com.ecommerce.ecommerce_backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Admin Dashboard", description = "Administrative dashboard metrics and summaries")
@RestController
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "Get administrative dashboard summary")
    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }
}