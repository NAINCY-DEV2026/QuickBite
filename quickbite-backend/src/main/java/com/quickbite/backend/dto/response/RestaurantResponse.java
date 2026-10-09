package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String phone;
    private String email;
    private String cuisineType;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private BigDecimal deliveryFee;
    private BigDecimal taxPercentage;
    private String image;
    private BigDecimal rating;
    private Boolean isActive;
    private Boolean openNow;   // active and inside opening hours right now
}