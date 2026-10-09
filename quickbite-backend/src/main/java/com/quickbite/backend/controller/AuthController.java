package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.ForgotPasswordRequest;
import com.quickbite.backend.dto.request.LoginRequest;
import com.quickbite.backend.dto.request.RegisterRequest;
import com.quickbite.backend.dto.request.ResetPasswordRequest;
import com.quickbite.backend.dto.response.LoginResponse;
import com.quickbite.backend.service.AuthService;
import com.quickbite.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registerCustomer(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request, "CUSTOMER");
    }

    @PostMapping("/register-owner")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registerOwner(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request, "RESTAURANT_OWNER");
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return Map.of("message", "If an account exists with this email, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return Map.of("message", "Password reset successfully. You can now log in.");
    }
}