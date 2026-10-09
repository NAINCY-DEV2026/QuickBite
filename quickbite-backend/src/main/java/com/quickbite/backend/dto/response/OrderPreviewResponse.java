package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderPreviewResponse {
    private String restaurantName;
    private int itemCount;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String couponCode; // the coupon that was applied, if any
}