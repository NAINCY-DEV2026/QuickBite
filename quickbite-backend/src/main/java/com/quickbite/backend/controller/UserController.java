package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.ChangePasswordRequest;
import com.quickbite.backend.dto.request.DeactivateAccountRequest;
import com.quickbite.backend.dto.request.UpdateProfileRequest;
import com.quickbite.backend.dto.response.UserResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping("/me")
    public UserResponse getMyProfile() {
        return userService.getProfile(currentUserUtil.getCurrentUserId());
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUserUtil.getCurrentUserId(), request);
    }

    @PutMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUserUtil.getCurrentUserId(), request);
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadProfileImage(@RequestParam("file") MultipartFile file) {
        return userService.uploadProfileImage(currentUserUtil.getCurrentUserId(), file);
    }

    @PutMapping("/deactivate")
    public void deactivateAccount(@Valid @RequestBody DeactivateAccountRequest request) {
        userService.deactivateAccount(currentUserUtil.getCurrentUserId(), request.getPassword());
    }
}