package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull private Long orderId;
    @NotBlank private String paymentMethod;
}