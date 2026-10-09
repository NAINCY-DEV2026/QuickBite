package com.quickbite.backend.repository;

import com.quickbite.backend.dto.response.PopularFoodResponse;
import com.quickbite.backend.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
    boolean existsByFoodItemId(Long foodItemId);

    @Query("""
        select new com.quickbite.backend.dto.response.PopularFoodResponse(
            f.id, f.name, r.name, sum(oi.quantity), sum(oi.totalPrice))
        from OrderItem oi
            join oi.foodItem f
            join oi.order o
            join o.restaurant r
        where o.orderStatus = com.quickbite.backend.enums.OrderStatus.DELIVERED
        group by f.id, f.name, r.name
        order by sum(oi.quantity) desc
        """)
    List<PopularFoodResponse> popularFoods(Pageable pageable);
}