package com.quickbite.backend.entity;

import com.quickbite.backend.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 8, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "max_discount", precision = 8, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "usage_limit", nullable = false)
    @Builder.Default
    private Integer usageLimit = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}