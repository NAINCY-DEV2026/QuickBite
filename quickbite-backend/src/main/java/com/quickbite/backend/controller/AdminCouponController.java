package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.CouponRequest;
import com.quickbite.backend.dto.response.CouponDetailResponse;
import com.quickbite.backend.service.CouponAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponAdminService couponAdminService;

    @GetMapping
    public Page<CouponDetailResponse> list(Pageable pageable) {
        return couponAdminService.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponDetailResponse create(@Valid @RequestBody CouponRequest request) {
        return couponAdminService.create(request);
    }

    @PutMapping("/{id}")
    public CouponDetailResponse update(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return couponAdminService.update(id, request);
    }

    @PutMapping("/{id}/active")
    public CouponDetailResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return couponAdminService.setActive(id, active);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean removed = couponAdminService.delete(id);
        return Map.of(
                "deleted", removed,
                "message", removed
                        ? "Coupon deleted."
                        : "This coupon was used in past orders, so it was disabled instead of deleted.");
    }
}