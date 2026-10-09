package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class RestaurantRequest {
    @NotBlank private String name;
    private String description;
    private String address;
    @NotBlank private String city;
    private String phone;
    private String email;
    private String cuisineType;
    private LocalTime openingTime;
    private LocalTime closingTime;
    @NotNull @PositiveOrZero private BigDecimal deliveryFee;
    @NotNull @PositiveOrZero private BigDecimal taxPercentage;
}