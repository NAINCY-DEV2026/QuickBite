package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.ReviewRequest;
import com.quickbite.backend.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse addReview(Long customerId, ReviewRequest request);
    Page<ReviewResponse> getByRestaurant(Long restaurantId, Pageable pageable);
}