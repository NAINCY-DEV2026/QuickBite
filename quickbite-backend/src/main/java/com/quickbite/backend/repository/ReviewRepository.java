package com.quickbite.backend.repository;

import com.quickbite.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);
    Optional<Review> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}