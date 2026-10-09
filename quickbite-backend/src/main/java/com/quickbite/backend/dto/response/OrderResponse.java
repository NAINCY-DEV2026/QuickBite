package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String couponCode;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}