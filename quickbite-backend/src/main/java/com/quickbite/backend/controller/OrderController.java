package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.OrderPreviewRequest;
import com.quickbite.backend.dto.request.OrderRequest;
import com.quickbite.backend.dto.request.UpdateOrderStatusRequest;
import com.quickbite.backend.dto.response.OrderPreviewResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserUtil currentUserUtil;

    // Price breakdown (subtotal, delivery, tax, discount, total) without placing the order
    @PostMapping("/api/orders/preview")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderPreviewResponse preview(@RequestBody(required = false) OrderPreviewRequest request) {
        String couponCode = request == null ? null : request.getCouponCode();
        return orderService.previewOrder(currentUserUtil.getCurrentUserId(), couponCode);
    }

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.placeOrder(currentUserUtil.getCurrentUserId(), request);
    }

    @GetMapping("/api/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        return orderService.getCustomerOrders(currentUserUtil.getCurrentUserId(), pageable);
    }

    // Customer who placed it, the owner of that restaurant, or an admin
    @GetMapping("/api/orders/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(currentUserUtil.getCurrentUserId(), currentUserUtil.getCurrentUserRole(), id);
    }

    @PutMapping("/api/orders/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancelOrder(currentUserUtil.getCurrentUserId(), id);
    }

    @GetMapping("/api/restaurant/{restaurantId}/orders")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public Page<OrderResponse> getRestaurantOrders(@PathVariable Long restaurantId, Pageable pageable) {
        return orderService.getRestaurantOrders(currentUserUtil.getCurrentUserId(), restaurantId, pageable);
    }

    @PutMapping("/api/restaurant/orders/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(currentUserUtil.getCurrentUserId(), id, request.getStatus());
    }
}