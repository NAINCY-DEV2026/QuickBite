package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.response.ReviewResponse;
import com.quickbite.backend.entity.Review;

public class ReviewMapper {

    public static ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .customerName(r.getCustomer().getName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}