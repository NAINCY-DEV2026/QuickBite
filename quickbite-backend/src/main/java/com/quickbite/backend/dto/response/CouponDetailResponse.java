package com.quickbite.backend.dto.response;

import com.quickbite.backend.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CouponDetailResponse {
    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Integer usageLimit;
    private long usedCount;
    private Boolean isActive;
    private String status;   // ACTIVE, DISABLED, SCHEDULED, EXPIRED, EXHAUSTED
}