package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String phone;

    // BCrypt only uses the first 72 bytes, so longer passwords are rejected up front
    @NotBlank
    @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
    private String password;

    @NotBlank
    private String confirmPassword;
}