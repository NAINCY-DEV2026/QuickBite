package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class PopularFoodResponse {
    private Long foodId;
    private String name;
    private String restaurantName;
    private Long quantitySold;
    private BigDecimal revenue;
}