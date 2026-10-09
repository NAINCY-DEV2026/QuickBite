package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.response.PaymentResponse;
import com.quickbite.backend.entity.Payment;

public class PaymentMapper {

    public static PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod().name())
                .paymentStatus(p.getPaymentStatus().name())
                .transactionRef(p.getTransactionRef())
                .build();
    }
}