package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeactivateAccountRequest {
    @NotBlank(message = "Enter your password to confirm")
    private String password;
}