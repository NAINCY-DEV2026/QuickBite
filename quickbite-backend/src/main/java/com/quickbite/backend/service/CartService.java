package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.AddCartItemRequest;
import com.quickbite.backend.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long customerId);
    CartResponse addItem(Long customerId, AddCartItemRequest request);
    CartResponse updateItemQuantity(Long customerId, Long cartItemId, int quantity);
    CartResponse removeItem(Long customerId, Long cartItemId);
    void clearCart(Long customerId);
}