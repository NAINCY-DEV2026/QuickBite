package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalRestaurants;
    private long activeRestaurants;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long ordersInProgress;
    private BigDecimal totalRevenue;        // delivered orders only
    private BigDecimal averageOrderValue;
}