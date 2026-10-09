package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.LoginRequest;
import com.quickbite.backend.dto.request.RegisterRequest;
import com.quickbite.backend.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse register(RegisterRequest request, String role);
    LoginResponse login(LoginRequest request);
}