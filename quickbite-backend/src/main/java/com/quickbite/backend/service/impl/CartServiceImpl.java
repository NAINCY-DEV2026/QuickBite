package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.AddCartItemRequest;
import com.quickbite.backend.dto.response.CartResponse;
import com.quickbite.backend.entity.Cart;
import com.quickbite.backend.entity.CartItem;
import com.quickbite.backend.entity.FoodItem;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.exception.CartException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.mapper.CartMapper;
import com.quickbite.backend.repository.CartItemRepository;
import com.quickbite.backend.repository.CartRepository;
import com.quickbite.backend.repository.FoodItemRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int MAX_QTY_PER_ITEM = 20;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse getCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        return view(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long customerId, AddCartItemRequest request) {
        if (request.getQuantity() > MAX_QTY_PER_ITEM) {
            throw new CartException("You can order at most " + MAX_QTY_PER_ITEM + " of one item");
        }

        Cart cart = getOrCreateCart(customerId);

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found"));

        if (!Boolean.TRUE.equals(foodItem.getRestaurant().getIsActive())) {
            throw new CartException("This restaurant is not accepting orders right now");
        }
        if (!Boolean.TRUE.equals(foodItem.getIsAvailable())) {
            throw new CartException("This food item is currently unavailable");
        }

        // An empty cart is not tied to any restaurant (e.g. after the last item was removed)
        if (cartItemRepository.countByCartId(cart.getId()) == 0) {
            cart.setRestaurant(null);
        }

        // THE CORE BUSINESS RULE: a cart cannot mix restaurants
        if (cart.getRestaurant() != null
                && !cart.getRestaurant().getId().equals(foodItem.getRestaurant().getId())) {
            throw new CartException("Your cart already contains items from "
                    + cart.getRestaurant().getName()
                    + ". Clear your cart to order from a different restaurant.");
        }
        if (cart.getRestaurant() == null) {
            cart.setRestaurant(foodItem.getRestaurant());
        }
        cartRepository.save(cart);

        Optional<CartItem> existing = cartItemRepository.findByCartIdAndFoodItemId(cart.getId(), foodItem.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > MAX_QTY_PER_ITEM) {
                throw new CartException("You can order at most " + MAX_QTY_PER_ITEM + " of one item");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            cartItemRepository.save(CartItem.builder()
                    .cart(cart)
                    .foodItem(foodItem)
                    .quantity(request.getQuantity())
                    .build());
        }

        return view(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long customerId, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CartException("This item does not belong to your cart");
        }
        if (quantity > MAX_QTY_PER_ITEM) {
            throw new CartException("You can order at most " + MAX_QTY_PER_ITEM + " of one item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            cartItemRepository.flush();
            // Last item gone: release the cart from its restaurant
            if (cartItemRepository.countByCartId(cart.getId()) == 0) {
                cart.setRestaurant(null);
                cartRepository.save(cart);
            }
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return view(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long customerId, Long cartItemId) {
        return updateItemQuantity(customerId, cartItemId, 0);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        cartItemRepository.deleteAll(cartItemRepository.findByCartId(cart.getId()));
        cart.setRestaurant(null);
        cartRepository.save(cart);
    }

    // ------------------------------------------------------------ helpers

    private CartResponse view(Cart cart) {
        cartItemRepository.flush();
        return CartMapper.toResponse(cart, cartItemRepository.findByCartId(cart.getId()));
    }

    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return cartRepository.save(Cart.builder().customer(customer).build());
                });
    }
}