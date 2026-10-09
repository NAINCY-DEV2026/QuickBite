package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.FoodItemRequest;
import com.quickbite.backend.dto.response.FoodItemResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.FoodItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;
    private final CurrentUserUtil currentUserUtil;

    // GET /api/foods?category=Pizza&minPrice=100&maxPrice=500&vegetarian=true&q=cheese&sort=price,asc
    @GetMapping("/api/foods")
    public Page<FoodItemResponse> search(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean vegetarian,
            Pageable pageable) {
        return foodItemService.search(restaurantId, q, category, minPrice, maxPrice, vegetarian, pageable);
    }

    @GetMapping("/api/restaurants/{restaurantId}/foods")
    public List<FoodItemResponse> getByRestaurant(@PathVariable Long restaurantId) {
        return foodItemService.getByRestaurant(restaurantId);
    }

    @PostMapping("/api/restaurants/{restaurantId}/foods")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public FoodItemResponse create(@PathVariable Long restaurantId, @Valid @RequestBody FoodItemRequest request) {
        return foodItemService.create(currentUserUtil.getCurrentUserId(), restaurantId, request);
    }

    @PutMapping("/api/foods/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public FoodItemResponse update(@PathVariable Long id, @Valid @RequestBody FoodItemRequest request) {
        return foodItemService.update(currentUserUtil.getCurrentUserId(), id, request);
    }

    @PostMapping(value = "/api/foods/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public FoodItemResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return foodItemService.updateImage(currentUserUtil.getCurrentUserId(), id, file);
    }

    @DeleteMapping("/api/foods/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean removed = foodItemService.delete(currentUserUtil.getCurrentUserId(), id);
        return Map.of(
                "deleted", removed,
                "message", removed
                        ? "Dish deleted."
                        : "This dish appears in past orders, so it was marked unavailable instead of deleted.");
    }

    @PatchMapping("/api/foods/{id}/availability")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public void setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        foodItemService.setAvailability(currentUserUtil.getCurrentUserId(), id, available);
    }
}