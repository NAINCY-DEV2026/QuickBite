package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Constructor argument order matters: it is called directly from a JPQL "select new" expression
@Data @NoArgsConstructor @AllArgsConstructor
public class TopRestaurantResponse {
    private Long restaurantId;
    private String name;
    private Long orders;
    private BigDecimal revenue;
}