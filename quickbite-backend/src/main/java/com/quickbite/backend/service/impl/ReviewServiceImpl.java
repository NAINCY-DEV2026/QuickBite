package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.ReviewRequest;
import com.quickbite.backend.dto.response.ReviewResponse;
import com.quickbite.backend.entity.Order;
import com.quickbite.backend.entity.Restaurant;
import com.quickbite.backend.entity.Review;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.UnauthorizedException;
import com.quickbite.backend.mapper.ReviewMapper;
import com.quickbite.backend.repository.OrderRepository;
import com.quickbite.backend.repository.RestaurantRepository;
import com.quickbite.backend.repository.ReviewRepository;
import com.quickbite.backend.service.NotificationService;
import com.quickbite.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewResponse addReview(Long customerId, ReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("This is not your order");
        }
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can only review delivered orders");
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("You have already reviewed this order");
        }

        Review review = Review.builder()
                .customer(order.getCustomer())
                .restaurant(order.getRestaurant())
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        recalculateRestaurantRating(order.getRestaurant().getId());

        notificationService.notify(order.getRestaurant().getOwner().getId(),
                "New " + request.getRating() + "-star review for " + order.getRestaurant().getName()
                        + " from " + order.getCustomer().getName() + ".", "REVIEW");

        return ReviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getByRestaurant(Long restaurantId, Pageable pageable) {
        return reviewRepository.findByRestaurantId(restaurantId, pageable).map(ReviewMapper::toResponse);
    }

    private void recalculateRestaurantRating(Long restaurantId) {
        List<Review> allReviews = reviewRepository
                .findByRestaurantId(restaurantId, Pageable.unpaged())
                .getContent();

        if (allReviews.isEmpty()) return;

        BigDecimal sum = allReviews.stream()
                .map(r -> BigDecimal.valueOf(r.getRating()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(BigDecimal.valueOf(allReviews.size()), 2, RoundingMode.HALF_UP);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurant.setRating(average);
        restaurantRepository.save(restaurant);
    }
}