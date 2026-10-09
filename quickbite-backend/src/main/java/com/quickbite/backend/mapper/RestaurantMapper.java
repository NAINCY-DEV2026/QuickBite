package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.request.RestaurantRequest;
import com.quickbite.backend.dto.response.RestaurantResponse;
import com.quickbite.backend.entity.Restaurant;

import java.time.LocalTime;

public class RestaurantMapper {

    public static RestaurantResponse toResponse(Restaurant r) {
        boolean active = Boolean.TRUE.equals(r.getIsActive());
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .address(r.getAddress())
                .city(r.getCity())
                .phone(r.getPhone())
                .email(r.getEmail())
                .cuisineType(r.getCuisineType())
                .openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime())
                .deliveryFee(r.getDeliveryFee())
                .taxPercentage(r.getTaxPercentage())
                .image(r.getImage())
                .rating(r.getRating())
                .isActive(r.getIsActive())
                .openNow(active && isOpenAt(LocalTime.now(), r.getOpeningTime(), r.getClosingTime()))
                .build();
    }

    // Same rule as the openNow filter in RestaurantRepository.search
    public static boolean isOpenAt(LocalTime now, LocalTime open, LocalTime close) {
        if (open == null || close == null) return true;
        if (close.isAfter(open)) {
            return !now.isBefore(open) && now.isBefore(close);
        }
        return !now.isBefore(open) || now.isBefore(close); // overnight window
    }

    // Used when CREATING a restaurant. Owner is set separately in the service
    // (never trust an "owner" field coming from the client).
    public static Restaurant toEntity(RestaurantRequest req) {
        return Restaurant.builder()
                .name(req.getName())
                .description(req.getDescription())
                .address(req.getAddress())
                .city(req.getCity())
                .phone(req.getPhone())
                .email(req.getEmail())
                .cuisineType(req.getCuisineType())
                .openingTime(req.getOpeningTime())
                .closingTime(req.getClosingTime())
                .deliveryFee(req.getDeliveryFee())
                .taxPercentage(req.getTaxPercentage())
                .build();
    }

    // Used when UPDATING: mutate the existing managed entity instead of building a new one
    public static void updateEntity(Restaurant r, RestaurantRequest req) {
        r.setName(req.getName());
        r.setDescription(req.getDescription());
        r.setAddress(req.getAddress());
        r.setCity(req.getCity());
        r.setPhone(req.getPhone());
        r.setEmail(req.getEmail());
        r.setCuisineType(req.getCuisineType());
        r.setOpeningTime(req.getOpeningTime());
        r.setClosingTime(req.getClosingTime());
        r.setDeliveryFee(req.getDeliveryFee());
        r.setTaxPercentage(req.getTaxPercentage());
    }
}