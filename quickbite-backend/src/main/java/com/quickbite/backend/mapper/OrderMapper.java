package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.response.OrderItemResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import com.quickbite.backend.entity.Order;
import com.quickbite.backend.entity.OrderItem;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderMapper {

    public static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .foodName(item.getFoodNameSnapshot())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .customerName(order.getCustomer().getName())
                .customerPhone(order.getCustomer().getPhone())
                .deliveryAddress(formatAddress(order))
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null)
                .items(order.getOrderItems().stream().map(OrderMapper::toItemResponse).toList())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    // Built from the address snapshot stored on the order, never from the live Address row
    private static String formatAddress(Order o) {
        String base = Stream.of(
                        o.getDeliveryHouseFlat(), o.getDeliveryStreet(), o.getDeliveryArea(),
                        o.getDeliveryCity(), o.getDeliveryState(), o.getDeliveryPincode())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
        if (o.getDeliveryLandmark() != null && !o.getDeliveryLandmark().isBlank()) {
            base += " (Landmark: " + o.getDeliveryLandmark() + ")";
        }
        return base;
    }
}