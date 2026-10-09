package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long foodItemId;
    private String foodName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}