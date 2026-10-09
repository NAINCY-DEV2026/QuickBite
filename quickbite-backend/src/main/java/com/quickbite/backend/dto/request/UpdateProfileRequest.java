package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String phone;
}