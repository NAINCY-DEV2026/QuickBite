package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.response.CartItemResponse;
import com.quickbite.backend.dto.response.CartResponse;
import com.quickbite.backend.entity.Cart;
import com.quickbite.backend.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    public static CartItemResponse toItemResponse(CartItem item) {
        BigDecimal unitPrice = item.getFoodItem().getPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .foodItemId(item.getFoodItem().getId())
                .foodName(item.getFoodItem().getName())
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .totalPrice(total)
                .build();
    }

    public static CartResponse toResponse(Cart cart) {
        return toResponse(cart, cart.getCartItems());
    }

    // The service passes freshly queried items, because the in-memory collection can be stale
    // right after an add/remove inside the same transaction.
    public static CartResponse toResponse(Cart cart, List<CartItem> cartItems) {
        List<CartItemResponse> items = cartItems.stream()
                .map(CartMapper::toItemResponse)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .restaurantId(items.isEmpty() || cart.getRestaurant() == null ? null : cart.getRestaurant().getId())
                .restaurantName(items.isEmpty() || cart.getRestaurant() == null ? null : cart.getRestaurant().getName())
                .items(items)
                .subtotal(subtotal)
                .build();
    }
}