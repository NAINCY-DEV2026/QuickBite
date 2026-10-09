package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long restaurantId;
    private String restaurantName;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
}