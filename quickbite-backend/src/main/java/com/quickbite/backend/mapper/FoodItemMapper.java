package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.request.FoodItemRequest;
import com.quickbite.backend.dto.response.FoodItemResponse;
import com.quickbite.backend.entity.Category;
import com.quickbite.backend.entity.FoodItem;
import com.quickbite.backend.entity.Restaurant;

public class FoodItemMapper {

    public static FoodItemResponse toResponse(FoodItem f) {
        return FoodItemResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .description(f.getDescription())
                .price(f.getPrice())
                .image(f.getImage())
                .categoryId(f.getCategory().getId())
                .categoryName(f.getCategory().getName())
                .isVeg(f.getIsVeg())
                .isAvailable(f.getIsAvailable())
                .restaurantId(f.getRestaurant().getId())
                .restaurantName(f.getRestaurant().getName())
                .build();
    }

    public static FoodItem toEntity(FoodItemRequest req, Restaurant restaurant, Category category) {
        return FoodItem.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .restaurant(restaurant)
                .category(category)
                .isVeg(req.getIsVeg() != null ? req.getIsVeg() : true)
                .isAvailable(req.getIsAvailable() != null ? req.getIsAvailable() : true)
                .build();
    }

    public static void updateEntity(FoodItem f, FoodItemRequest req, Category category) {
        f.setName(req.getName());
        f.setDescription(req.getDescription());
        f.setPrice(req.getPrice());
        f.setCategory(category);
        if (req.getIsVeg() != null) f.setIsVeg(req.getIsVeg());
        if (req.getIsAvailable() != null) f.setIsAvailable(req.getIsAvailable());
    }
}