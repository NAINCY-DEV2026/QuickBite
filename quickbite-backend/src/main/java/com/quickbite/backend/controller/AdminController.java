package com.quickbite.backend.controller;

import com.quickbite.backend.dto.response.AdminStatsResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import com.quickbite.backend.dto.response.ReportResponse;
import com.quickbite.backend.dto.response.RestaurantResponse;
import com.quickbite.backend.dto.response.UserResponse;
import com.quickbite.backend.service.AdminService;
import com.quickbite.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

// Every /api/admin/** route is restricted to ROLE_ADMIN in SecurityConfig
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/reports")
    public ReportResponse getReport(@RequestParam(defaultValue = "7") int days) {
        return reportService.getReport(days);
    }

    @GetMapping("/users")
    public Page<UserResponse> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        return adminService.getUsers(q, role, pageable);
    }

    @PutMapping("/users/{id}/block")
    public void blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
    }

    @PutMapping("/users/{id}/unblock")
    public void unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
    }

    @GetMapping("/restaurants")
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        return adminService.getAllRestaurants(pageable);
    }

    @PutMapping("/restaurants/{id}/block")
    public void blockRestaurant(@PathVariable Long id) {
        adminService.blockRestaurant(id);
    }

    @PutMapping("/restaurants/{id}/unblock")
    public void unblockRestaurant(@PathVariable Long id) {
        adminService.unblockRestaurant(id);
    }

    @GetMapping("/orders")
    public Page<OrderResponse> getOrders(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return adminService.getOrders(status, pageable);
    }
}