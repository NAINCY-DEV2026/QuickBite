package com.quickbite.backend.service;

import com.quickbite.backend.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse getByOrderId(Long requesterId, String requesterRole, Long orderId);
    PaymentResponse markAsPaid(Long ownerId, Long orderId);
    PaymentResponse markAsFailed(Long ownerId, Long orderId);
}