package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.response.PaymentResponse;
import com.quickbite.backend.entity.Order;
import com.quickbite.backend.entity.Payment;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.enums.PaymentStatus;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.InvalidOrderStateException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.UnauthorizedException;
import com.quickbite.backend.mapper.PaymentMapper;
import com.quickbite.backend.repository.PaymentRepository;
import com.quickbite.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(Long requesterId, String requesterRole, Long orderId) {
        Payment payment = findOrThrow(orderId);
        Order order = payment.getOrder();

        boolean allowed = "ADMIN".equals(requesterRole)
                || order.getCustomer().getId().equals(requesterId)
                || order.getRestaurant().getOwner().getId().equals(requesterId);
        if (!allowed) {
            throw new UnauthorizedException("You cannot view this payment");
        }
        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse markAsPaid(Long ownerId, Long orderId) {
        Payment payment = findOrThrow(orderId);
        Order order = payment.getOrder();
        assertOwner(order, ownerId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.REJECTED) {
            throw new InvalidOrderStateException("Cannot collect payment for a " + order.getOrderStatus() + " order");
        }
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment is already " + payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse markAsFailed(Long ownerId, Long orderId) {
        Payment payment = findOrThrow(orderId);
        Order order = payment.getOrder();
        assertOwner(order, ownerId);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only a pending payment can be marked failed");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        order.setPaymentStatus(PaymentStatus.FAILED);
        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    private Payment findOrThrow(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this order"));
    }

    private void assertOwner(Order order, Long ownerId) {
        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not manage this restaurant's orders");
        }
    }
}