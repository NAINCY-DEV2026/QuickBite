package com.quickbite.backend.dto.request;

import com.quickbite.backend.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$", message = "Code must be 3 to 30 letters, digits, - or _")
    private String code;

    @Size(max = 255)
    private String description;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Positive
    private BigDecimal discountValue;

    @NotNull
    @PositiveOrZero
    private BigDecimal minOrderAmount;

    @PositiveOrZero
    private BigDecimal maxDiscount;      // optional, used for percentage coupons only

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime expiryDate;

    @NotNull
    @Positive
    private Integer usageLimit;

    private Boolean isActive;            // null = keep current (update) / active (create)

    private Boolean notifyCustomers;     // create only
}