package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank private String status;
}