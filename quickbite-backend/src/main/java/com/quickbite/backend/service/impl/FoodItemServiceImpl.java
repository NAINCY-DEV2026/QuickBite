package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.FoodItemRequest;
import com.quickbite.backend.dto.response.FoodItemResponse;
import com.quickbite.backend.entity.Category;
import com.quickbite.backend.entity.FoodItem;
import com.quickbite.backend.entity.Restaurant;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.UnauthorizedException;
import com.quickbite.backend.mapper.FoodItemMapper;
import com.quickbite.backend.repository.*;
import com.quickbite.backend.service.FileStorageService;
import com.quickbite.backend.service.FoodItemService;
import com.quickbite.backend.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FoodItemServiceImpl implements FoodItemService {

    private static final Set<String> SORTABLE = Set.of("price", "name", "restaurant.rating");
    private static final int MAX_PAGE_SIZE = 50;

    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public FoodItemResponse create(Long ownerId, Long restaurantId, FoodItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        assertOwnership(restaurant, ownerId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        FoodItem foodItem = FoodItemMapper.toEntity(request, restaurant, category);
        return FoodItemMapper.toResponse(foodItemRepository.save(foodItem));
    }

    @Override
    @Transactional
    public FoodItemResponse update(Long ownerId, Long foodItemId, FoodItemRequest request) {
        FoodItem foodItem = findOwnedFood(ownerId, foodItemId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        FoodItemMapper.updateEntity(foodItem, request, category);
        return FoodItemMapper.toResponse(foodItemRepository.save(foodItem));
    }

    @Override
    @Transactional
    public boolean delete(Long ownerId, Long foodItemId) {
        FoodItem foodItem = findOwnedFood(ownerId, foodItemId);

        // Past orders reference this dish. They keep their own name/price snapshot, but the row
        // must stay for the foreign key, so hide it from ordering instead of deleting it.
        if (orderItemRepository.existsByFoodItemId(foodItemId)) {
            foodItem.setIsAvailable(false);
            foodItemRepository.save(foodItem);
            return false;
        }

        cartItemRepository.deleteByFoodItemId(foodItemId); // remove it from anyone's cart first
        String image = foodItem.getImage();
        foodItemRepository.delete(foodItem);
        TransactionUtils.afterCommit(() -> fileStorageService.delete(image));
        return true;
    }

    @Override
    @Transactional
    public void setAvailability(Long ownerId, Long foodItemId, boolean available) {
        FoodItem foodItem = findOwnedFood(ownerId, foodItemId);
        foodItem.setIsAvailable(available);
        foodItemRepository.save(foodItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getByRestaurant(Long restaurantId) {
        return foodItemRepository.findByRestaurantId(restaurantId).stream()
                .map(FoodItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FoodItemResponse> search(Long restaurantId, String q, String category,
                                         BigDecimal minPrice, BigDecimal maxPrice, boolean vegOnly, Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }
        return foodItemRepository
                .search(restaurantId, clean(q), clean(category), minPrice, maxPrice, vegOnly, sanitize(pageable))
                .map(FoodItemMapper::toResponse);
    }

    @Override
    @Transactional
    public FoodItemResponse updateImage(Long ownerId, Long foodItemId, MultipartFile file) {
        FoodItem foodItem = findOwnedFood(ownerId, foodItemId);
        String oldImage = foodItem.getImage();

        foodItem.setImage(fileStorageService.store(file, "foods"));
        FoodItem saved = foodItemRepository.save(foodItem);

        TransactionUtils.afterCommit(() -> fileStorageService.delete(oldImage));
        return FoodItemMapper.toResponse(saved);
    }

    // ------------------------------------------------------------ helpers

    private FoodItem findOwnedFood(Long ownerId, Long foodItemId) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found"));
        assertOwnership(foodItem.getRestaurant(), ownerId);
        return foodItem;
    }

    private void assertOwnership(Restaurant restaurant, Long ownerId) {
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not own this restaurant");
        }
    }

    private Pageable sanitize(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new BadRequestException("Cannot sort by '" + order.getProperty() + "'");
            }
        }
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("name");
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(pageable.getPageNumber(), 0), size, sort.and(Sort.by("id")));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}