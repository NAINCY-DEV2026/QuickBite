package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.ChangePasswordRequest;
import com.quickbite.backend.dto.request.UpdateProfileRequest;
import com.quickbite.backend.dto.response.UserResponse;
import com.quickbite.backend.entity.Restaurant;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.enums.AccountStatus;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.enums.Role;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.mapper.UserMapper;
import com.quickbite.backend.repository.OrderRepository;
import com.quickbite.backend.repository.RestaurantRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.FileStorageService;
import com.quickbite.backend.service.UserService;
import com.quickbite.backend.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final List<OrderStatus> IN_PROGRESS = List.of(
            OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP, OrderStatus.OUT_FOR_DELIVERY);

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserMapper.toResponse(findUser(userId));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        // Only name and phone are editable here. Email, role and account status are never
        // taken from the client.
        user.setName(request.getName().trim());
        user.setPhone(request.getPhone().trim());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user {}", userId);
    }

    @Override
    @Transactional
    public UserResponse uploadProfileImage(Long userId, MultipartFile file) {
        User user = findUser(userId);
        String oldImage = user.getProfileImage();

        user.setProfileImage(fileStorageService.store(file, "profile-images"));
        User saved = userRepository.save(user);

        TransactionUtils.afterCommit(() -> fileStorageService.delete(oldImage));
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId, String password) {
        User user = findUser(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be deactivated");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password is incorrect");
        }

        boolean hasOrdersInProgress = user.getRole() == Role.CUSTOMER
                ? orderRepository.existsByCustomerIdAndOrderStatusIn(userId, IN_PROGRESS)
                : orderRepository.existsByRestaurantOwnerIdAndOrderStatusIn(userId, IN_PROGRESS);
        if (hasOrdersInProgress) {
            throw new BadRequestException("You still have orders in progress. Please wait until they are completed.");
        }

        // An owner who leaves must not keep taking orders that nobody will prepare
        if (user.getRole() == Role.RESTAURANT_OWNER) {
            List<Restaurant> owned = restaurantRepository.findByOwnerId(userId);
            owned.forEach(r -> r.setIsActive(false));
            restaurantRepository.saveAll(owned);
        }

        user.setAccountStatus(AccountStatus.DEACTIVATED);
        userRepository.save(user);
        log.info("Account {} deactivated", userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}