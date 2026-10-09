package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.CouponValidateRequest;
import com.quickbite.backend.dto.response.CouponResponse;
import com.quickbite.backend.entity.Coupon;
import com.quickbite.backend.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public CouponResponse validate(@Valid @RequestBody CouponValidateRequest request) {
        Coupon coupon = couponService.validateAndGet(request.getCode(), request.getOrderAmount());
        var discount = couponService.calculateDiscount(coupon, request.getOrderAmount());
        return CouponResponse.builder()
                .code(coupon.getCode())
                .discountAmount(discount)
                .valid(true)
                .message("Coupon applied successfully")
                .build();
    }
}