package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.ChangePasswordRequest;
import com.quickbite.backend.dto.request.UpdateProfileRequest;
import com.quickbite.backend.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    UserResponse uploadProfileImage(Long userId, MultipartFile file);
    void deactivateAccount(Long userId, String password);
}