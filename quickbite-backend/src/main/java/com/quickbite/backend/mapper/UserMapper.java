package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.response.UserResponse;
import com.quickbite.backend.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .accountStatus(user.getAccountStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}