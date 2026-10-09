package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.OrderRequest;
import com.quickbite.backend.dto.response.OrderPreviewResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderPreviewResponse previewOrder(Long customerId, String couponCode);
    OrderResponse placeOrder(Long customerId, OrderRequest request);
    OrderResponse getById(Long requesterId, String requesterRole, Long orderId);
    Page<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable);
    Page<OrderResponse> getRestaurantOrders(Long ownerId, Long restaurantId, Pageable pageable);
    OrderResponse updateStatus(Long ownerId, Long orderId, String newStatus);
    OrderResponse cancelOrder(Long customerId, Long orderId);
}