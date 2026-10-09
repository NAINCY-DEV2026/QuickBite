package com.quickbite.backend.repository;

import com.quickbite.backend.dto.response.TopRestaurantResponse;
import com.quickbite.backend.entity.Order;
import com.quickbite.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);
    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    List<Order> findByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus status);
    long countByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus status);
    long countByOrderStatus(OrderStatus status);

    long countByCouponIdAndOrderStatusNotIn(Long couponId, Collection<OrderStatus> statuses);
    boolean existsByCouponId(Long couponId);

    boolean existsByCustomerIdAndOrderStatusIn(Long customerId, Collection<OrderStatus> statuses);
    boolean existsByRestaurantOwnerIdAndOrderStatusIn(Long ownerId, Collection<OrderStatus> statuses);

    // ---------------- admin statistics and reports ----------------

    // Revenue = money from delivered orders only (null when there are none yet)
    @Query("""
        select sum(o.totalAmount) from Order o
        where o.orderStatus = com.quickbite.backend.enums.OrderStatus.DELIVERED
        """)
    BigDecimal totalRevenue();

    @Query("select o.orderStatus, count(o) from Order o group by o.orderStatus")
    List<Object[]> statusCounts();

    @Query("""
        select new com.quickbite.backend.dto.response.TopRestaurantResponse(
            r.id, r.name, count(o), sum(o.totalAmount))
        from Order o join o.restaurant r
        where o.orderStatus = com.quickbite.backend.enums.OrderStatus.DELIVERED
        group by r.id, r.name
        order by sum(o.totalAmount) desc
        """)
    List<TopRestaurantResponse> topRestaurants(Pageable pageable);

    // updatedAt is when the final DELIVERED status was set, so it doubles as the delivery time
    @Query("""
        select o.updatedAt, o.totalAmount from Order o
        where o.orderStatus = com.quickbite.backend.enums.OrderStatus.DELIVERED
          and o.updatedAt >= :since
        """)
    List<Object[]> deliveredSince(@Param("since") LocalDateTime since);
}