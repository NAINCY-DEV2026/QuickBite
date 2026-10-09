package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.FoodItemRequest;
import com.quickbite.backend.dto.response.FoodItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface FoodItemService {
    FoodItemResponse create(Long ownerId, Long restaurantId, FoodItemRequest request);
    FoodItemResponse update(Long ownerId, Long foodItemId, FoodItemRequest request);
    // true = row deleted, false = kept (and marked unavailable) because past orders reference it
    boolean delete(Long ownerId, Long foodItemId);
    void setAvailability(Long ownerId, Long foodItemId, boolean available);
    List<FoodItemResponse> getByRestaurant(Long restaurantId);
    Page<FoodItemResponse> search(Long restaurantId, String q, String category,
                                  BigDecimal minPrice, BigDecimal maxPrice, boolean vegOnly, Pageable pageable);
    FoodItemResponse updateImage(Long ownerId, Long foodItemId, MultipartFile file);
}