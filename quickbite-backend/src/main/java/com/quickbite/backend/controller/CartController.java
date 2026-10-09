package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.AddCartItemRequest;
import com.quickbite.backend.dto.response.CartResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart(currentUserUtil.getCurrentUserId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(currentUserUtil.getCurrentUserId(), request);
    }

    @PutMapping("/items/{id}")
    public CartResponse updateItem(@PathVariable Long id, @RequestParam int quantity) {
        return cartService.updateItemQuantity(currentUserUtil.getCurrentUserId(), id, quantity);
    }

    @DeleteMapping("/items/{id}")
    public CartResponse removeItem(@PathVariable Long id) {
        return cartService.removeItem(currentUserUtil.getCurrentUserId(), id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        cartService.clearCart(currentUserUtil.getCurrentUserId());
    }
}