package com.quickbite.backend.repository;

import com.quickbite.backend.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwnerId(Long ownerId);

    long countByIsActiveTrue();

    // One JPQL query handles every optional filter: a null parameter means "don't filter on this".
    // A derived-method name would need a separate method per filter combination.
    // Hours handle overnight windows (e.g. 18:00 to 02:00) too.
    @Query("""
        select r from Restaurant r
        where r.isActive = true
          and (:city is null or lower(r.city) like lower(concat('%', :city, '%')))
          and (:name is null or lower(r.name) like lower(concat('%', :name, '%')))
          and (:cuisine is null or lower(r.cuisineType) like lower(concat('%', :cuisine, '%')))
          and (:minRating is null or r.rating >= :minRating)
          and (:openNow = false
               or r.openingTime is null or r.closingTime is null
               or (r.closingTime > r.openingTime and :now >= r.openingTime and :now < r.closingTime)
               or (r.closingTime <= r.openingTime and (:now >= r.openingTime or :now < r.closingTime)))
        """)
    Page<Restaurant> search(@Param("city") String city,
                            @Param("name") String name,
                            @Param("cuisine") String cuisine,
                            @Param("minRating") BigDecimal minRating,
                            @Param("openNow") boolean openNow,
                            @Param("now") LocalTime now,
                            Pageable pageable);
}