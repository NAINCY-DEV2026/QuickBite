package com.quickbite.backend.service;

public interface EmailService {
    void sendRegistrationEmail(String toEmail, String name);
    void sendOrderConfirmationEmail(String toEmail, Long orderId, String totalAmount);
    void sendOrderCancellationEmail(String toEmail, Long orderId);
    void sendOrderDeliveredEmail(String toEmail, Long orderId, String restaurantName);
    void sendPasswordResetEmail(String toEmail, String resetLink);
}