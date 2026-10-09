package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.LoginRequest;
import com.quickbite.backend.dto.request.RegisterRequest;
import com.quickbite.backend.dto.response.LoginResponse;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.enums.Role;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.UserAlreadyExistsException;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.security.CustomUserDetails;
import com.quickbite.backend.security.JwtService;
import com.quickbite.backend.service.AuthService;
import com.quickbite.backend.service.EmailService;
import com.quickbite.backend.service.NotificationService;
import com.quickbite.backend.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request, String role) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("An account with this email already exists");
        }

        Role userRole = Role.valueOf(role);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        User saved = userRepository.save(user);

        String welcome = userRole == Role.RESTAURANT_OWNER
                ? "Welcome to QuickBite, " + saved.getName() + "! Create your first restaurant from the My Restaurant page."
                : "Welcome to QuickBite, " + saved.getName() + "! Browse restaurants and place your first order.";
        notificationService.notify(saved.getId(), welcome, "ACCOUNT");

        final String email = saved.getEmail();
        final String name = saved.getName();
        TransactionUtils.afterCommit(() -> emailService.sendRegistrationEmail(email, name));

        CustomUserDetails userDetails = new CustomUserDetails(saved);
        String token = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .role(saved.getRole().name())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // Throws AuthenticationException (handled globally) on bad credentials, blocked or deactivated accounts
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}