package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FoodItemRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @Positive private BigDecimal price;
    @NotNull private Long categoryId;
    private Boolean isVeg;
    private Boolean isAvailable;
}