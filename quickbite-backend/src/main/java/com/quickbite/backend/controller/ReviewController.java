package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.ReviewRequest;
import com.quickbite.backend.dto.response.ReviewResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserUtil currentUserUtil;

    @PostMapping("/api/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse addReview(@Valid @RequestBody ReviewRequest request) {
        return reviewService.addReview(currentUserUtil.getCurrentUserId(), request);
    }

    @GetMapping("/api/restaurants/{id}/reviews")
    public Page<ReviewResponse> getByRestaurant(@PathVariable Long id, Pageable pageable) {
        return reviewService.getByRestaurant(id, pageable);
    }
}