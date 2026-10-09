package com.quickbite.backend.service.impl;

import com.quickbite.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async   // runs on a background thread so a slow SMTP server never blocks an API request
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Override
    public void sendRegistrationEmail(String toEmail, String name) {
        send(toEmail, "Welcome to QuickBite!",
                "Hi " + name + ",\n\nThanks for registering with QuickBite. Start ordering your favorite food now!\n\n- Team QuickBite");
    }

    @Override
    public void sendOrderConfirmationEmail(String toEmail, Long orderId, String totalAmount) {
        send(toEmail, "Order Confirmed - #" + orderId,
                "Your order #" + orderId + " has been placed successfully.\nTotal amount: Rs." + totalAmount +
                "\n\nWe'll notify you as your order progresses.\n\n- Team QuickBite");
    }

    @Override
    public void sendOrderCancellationEmail(String toEmail, Long orderId) {
        send(toEmail, "Order Cancelled - #" + orderId,
                "Your order #" + orderId + " has been cancelled. Any online payment will be refunded.\n\n- Team QuickBite");
    }

    @Override
    public void sendOrderDeliveredEmail(String toEmail, Long orderId, String restaurantName) {
        send(toEmail, "Order Delivered - #" + orderId,
                "Your order #" + orderId + " from " + restaurantName + " has been delivered. Enjoy your meal!\n\n" +
                "Loved it? You can rate the restaurant from your Orders page.\n\n- Team QuickBite");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        send(toEmail, "Reset Your QuickBite Password",
                "We received a request to reset your password.\nClick the link below (valid for 15 minutes):\n" +
                resetLink + "\n\nIf you didn't request this, please ignore this email.\n\n- Team QuickBite");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} - subject: {}", to, subject);
        } catch (Exception e) {
            // Email is a side effect; its failure must never break registration/orders
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}