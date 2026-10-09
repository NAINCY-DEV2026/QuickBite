package com.quickbite.backend.controller;

import com.quickbite.backend.dto.response.PaymentResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping("/{orderId}")
    public PaymentResponse getByOrderId(@PathVariable Long orderId) {
        return paymentService.getByOrderId(
                currentUserUtil.getCurrentUserId(), currentUserUtil.getCurrentUserRole(), orderId);
    }

    @PutMapping("/{orderId}/mark-paid")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public PaymentResponse markAsPaid(@PathVariable Long orderId) {
        return paymentService.markAsPaid(currentUserUtil.getCurrentUserId(), orderId);
    }

    @PutMapping("/{orderId}/mark-failed")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public PaymentResponse markAsFailed(@PathVariable Long orderId) {
        return paymentService.markAsFailed(currentUserUtil.getCurrentUserId(), orderId);
    }
}