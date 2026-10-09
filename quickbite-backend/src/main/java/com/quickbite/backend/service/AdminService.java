package com.quickbite.backend.service;

import com.quickbite.backend.dto.response.AdminStatsResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import com.quickbite.backend.dto.response.RestaurantResponse;
import com.quickbite.backend.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminStatsResponse getStats();
    Page<UserResponse> getUsers(String q, String role, Pageable pageable);
    void blockUser(Long userId);
    void unblockUser(Long userId);
    Page<RestaurantResponse> getAllRestaurants(Pageable pageable);
    void blockRestaurant(Long restaurantId);
    void unblockRestaurant(Long restaurantId);
    Page<OrderResponse> getOrders(String status, Pageable pageable);
}