package com.quickbite.backend.repository;

import com.quickbite.backend.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByRestaurantId(Long restaurantId);

    long countByCategoryId(Long categoryId);

    // Dish search across restaurants: only available dishes from active restaurants.
    // Null parameter = filter not applied.
    @Query("""
        select f from FoodItem f
        where f.isAvailable = true
          and f.restaurant.isActive = true
          and (:restaurantId is null or f.restaurant.id = :restaurantId)
          and (:q is null or lower(f.name) like lower(concat('%', :q, '%')))
          and (:category is null or lower(f.category.name) = lower(:category))
          and (:minPrice is null or f.price >= :minPrice)
          and (:maxPrice is null or f.price <= :maxPrice)
          and (:vegOnly = false or f.isVeg = true)
        """)
    Page<FoodItem> search(@Param("restaurantId") Long restaurantId,
                          @Param("q") String q,
                          @Param("category") String category,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          @Param("vegOnly") boolean vegOnly,
                          Pageable pageable);
}