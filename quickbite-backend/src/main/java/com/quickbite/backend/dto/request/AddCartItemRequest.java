package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull private Long foodItemId;
    @NotNull @Positive private Integer quantity;
}