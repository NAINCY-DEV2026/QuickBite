package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.response.AdminStatsResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import com.quickbite.backend.dto.response.RestaurantResponse;
import com.quickbite.backend.dto.response.UserResponse;
import com.quickbite.backend.entity.Order;
import com.quickbite.backend.entity.Restaurant;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.enums.AccountStatus;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.enums.Role;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.mapper.OrderMapper;
import com.quickbite.backend.mapper.RestaurantMapper;
import com.quickbite.backend.mapper.UserMapper;
import com.quickbite.backend.repository.OrderRepository;
import com.quickbite.backend.repository.RestaurantRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.AdminService;
import com.quickbite.backend.service.NotificationService;
import com.quickbite.backend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private static final Set<String> USER_SORT = Set.of("createdAt", "name", "email");
    private static final Set<String> RESTAURANT_SORT = Set.of("createdAt", "name", "rating");
    private static final Set<String> ORDER_SORT = Set.of("createdAt", "totalAmount");
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long totalOrders = orderRepository.count();
        long completed = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);
        long rejected = orderRepository.countByOrderStatus(OrderStatus.REJECTED);

        BigDecimal revenue = orderRepository.totalRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;
        BigDecimal average = completed == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalRestaurants(restaurantRepository.count())
                .activeRestaurants(restaurantRepository.countByIsActiveTrue())
                .totalOrders(totalOrders)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .ordersInProgress(totalOrders - completed - cancelled - rejected)
                .totalRevenue(revenue)
                .averageOrderValue(average)
                .build();
    }

    // ------------------------------------------------------------ users

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String q, String role, Pageable pageable) {
        Pageable safe = PageUtils.sanitize(pageable, USER_SORT, NEWEST_FIRST);
        String query = (q == null || q.isBlank()) ? null : q.trim();

        Page<User> page = (role == null || role.isBlank())
                ? userRepository.search(query, safe)
                : userRepository.searchByRole(query, parseEnum(Role.class, role, "role"), safe);
        return page.map(UserMapper::toResponse);
    }

    @Override
    @Transactional
    public void blockUser(Long userId) {
        User user = findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be blocked");
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Only active accounts can be blocked");
        }
        user.setAccountStatus(AccountStatus.BLOCKED);
        userRepository.save(user);
        log.info("User {} blocked", userId);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        User user = findUser(userId);
        if (user.getAccountStatus() != AccountStatus.BLOCKED) {
            throw new BadRequestException("This account is not blocked");
        }
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("User {} unblocked", userId);
    }

    // ------------------------------------------------------------ restaurants

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        Pageable safe = PageUtils.sanitize(pageable, RESTAURANT_SORT, NEWEST_FIRST);
        return restaurantRepository.findAll(safe).map(RestaurantMapper::toResponse);
    }

    @Override
    @Transactional
    public void blockRestaurant(Long restaurantId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
        notificationService.notify(restaurant.getOwner().getId(),
                "Your restaurant \"" + restaurant.getName() + "\" was blocked by an administrator and is hidden from customers.",
                "ACCOUNT");
        log.info("Restaurant {} blocked", restaurantId);
    }

    @Override
    @Transactional
    public void unblockRestaurant(Long restaurantId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        restaurant.setIsActive(true);
        restaurantRepository.save(restaurant);
        notificationService.notify(restaurant.getOwner().getId(),
                "Your restaurant \"" + restaurant.getName() + "\" is live again.", "ACCOUNT");
        log.info("Restaurant {} unblocked", restaurantId);
    }

    // ------------------------------------------------------------ orders

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(String status, Pageable pageable) {
        Pageable safe = PageUtils.sanitize(pageable, ORDER_SORT, NEWEST_FIRST);
        Page<Order> page = (status == null || status.isBlank())
                ? orderRepository.findAll(safe)
                : orderRepository.findByOrderStatus(parseEnum(OrderStatus.class, status, "status"), safe);
        return page.map(OrderMapper::toResponse);
    }

    // ------------------------------------------------------------ helpers

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid " + field + ": " + value);
        }
    }
}