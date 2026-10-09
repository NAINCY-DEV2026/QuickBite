package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.RestaurantRequest;
import com.quickbite.backend.dto.response.RestaurantResponse;
import com.quickbite.backend.entity.Restaurant;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.UnauthorizedException;
import com.quickbite.backend.mapper.RestaurantMapper;
import com.quickbite.backend.repository.RestaurantRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.FileStorageService;
import com.quickbite.backend.service.RestaurantService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private static final Set<String> SORTABLE = Set.of("rating", "name", "createdAt", "deliveryFee");
    private static final int MAX_PAGE_SIZE = 50;

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public RestaurantResponse create(Long ownerId, RestaurantRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Restaurant restaurant = RestaurantMapper.toEntity(request);
        restaurant.setOwner(owner);
        return RestaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional
    public RestaurantResponse update(Long ownerId, Long restaurantId, RestaurantRequest request) {
        Restaurant restaurant = getOwnedRestaurantOrThrow(ownerId, restaurantId);
        RestaurantMapper.updateEntity(restaurant, request);
        return RestaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return RestaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> search(String city, String name, String cuisine,
                                           BigDecimal minRating, boolean openNow, Pageable pageable) {
        return restaurantRepository
                .search(clean(city), clean(name), clean(cuisine), minRating, openNow, LocalTime.now(), sanitize(pageable))
                .map(RestaurantMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(RestaurantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long ownerId, Long restaurantId) {
        Restaurant restaurant = getOwnedRestaurantOrThrow(ownerId, restaurantId);
        restaurant.setIsActive(false); // soft delete: orders and reviews keep pointing at it
        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse updateImage(Long ownerId, Long restaurantId, MultipartFile file) {
        Restaurant restaurant = getOwnedRestaurantOrThrow(ownerId, restaurantId);
        String oldImage = restaurant.getImage();

        restaurant.setImage(fileStorageService.store(file, "restaurants"));
        Restaurant saved = restaurantRepository.save(restaurant);

        TransactionUtils.afterCommit(() -> fileStorageService.delete(oldImage));
        return RestaurantMapper.toResponse(saved);
    }

    // ------------------------------------------------------------ helpers

    // An owner can only touch their own restaurant
    private Restaurant getOwnedRestaurantOrThrow(Long ownerId, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not own this restaurant");
        }
        return restaurant;
    }

    // Whitelists sortable fields (an unknown field would otherwise cause a 500), caps page size
    // and adds id as a tie-breaker so pagination order is stable.
    private Pageable sanitize(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new BadRequestException("Cannot sort by '" + order.getProperty() + "'");
            }
        }
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Order.desc("rating"), Sort.Order.asc("name"));
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(pageable.getPageNumber(), 0), size, sort.and(Sort.by("id")));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}