package com.quickbite.backend.service;

import com.quickbite.backend.entity.Coupon;
import java.math.BigDecimal;

public interface CouponService {
    Coupon validateAndGet(String code, BigDecimal orderAmount);
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount);
}