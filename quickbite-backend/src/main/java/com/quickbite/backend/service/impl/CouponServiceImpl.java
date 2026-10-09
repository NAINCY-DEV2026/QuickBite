package com.quickbite.backend.service.impl;

import com.quickbite.backend.entity.Coupon;
import com.quickbite.backend.enums.DiscountType;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.exception.InvalidCouponException;
import com.quickbite.backend.repository.CouponRepository;
import com.quickbite.backend.repository.OrderRepository;
import com.quickbite.backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    @Override
    public Coupon validateAndGet(String code, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new InvalidCouponException("Invalid coupon code"));

        if (!coupon.getIsActive()) {
            throw new InvalidCouponException("This coupon is no longer active");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getExpiryDate())) {
            throw new InvalidCouponException("This coupon has expired or is not yet valid");
        }
        if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new InvalidCouponException(
                "Minimum order amount for this coupon is " + coupon.getMinOrderAmount());
        }

        // Cancelled/rejected orders do not consume a coupon use
        long used = orderRepository.countByCouponIdAndOrderStatusNotIn(
                coupon.getId(), List.of(OrderStatus.CANCELLED, OrderStatus.REJECTED));
        if (used >= coupon.getUsageLimit()) {
            throw new InvalidCouponException("This coupon has reached its usage limit");
        }
        return coupon;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }
        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount; // discount can never exceed the order value
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}