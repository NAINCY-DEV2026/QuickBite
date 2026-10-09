package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull private Long addressId;
    private String couponCode;
    @NotBlank private String paymentMethod; // COD / CARD_SIMULATION / UPI_SIMULATION
}