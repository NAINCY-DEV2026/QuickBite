package com.quickbite.backend.service.impl;

import com.quickbite.backend.entity.PasswordResetToken;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.repository.PasswordResetTokenRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.EmailService;
import com.quickbite.backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void forgotPassword(String email) {
        // Same behaviour whether or not the email exists, to avoid account enumeration
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            tokenRepository.save(PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiryDate(LocalDateTime.now().plusMinutes(15))
                    .build());

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("This reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken); // single use
    }
}