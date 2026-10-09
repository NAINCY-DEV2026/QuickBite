package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.RestaurantRequest;
import com.quickbite.backend.dto.response.RestaurantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface RestaurantService {
    RestaurantResponse create(Long ownerId, RestaurantRequest request);
    RestaurantResponse update(Long ownerId, Long restaurantId, RestaurantRequest request);
    RestaurantResponse getById(Long id);
    Page<RestaurantResponse> search(String city, String name, String cuisine,
                                    BigDecimal minRating, boolean openNow, Pageable pageable);
    List<RestaurantResponse> getByOwner(Long ownerId);
    void delete(Long ownerId, Long restaurantId);
    RestaurantResponse updateImage(Long ownerId, Long restaurantId, MultipartFile file);
}