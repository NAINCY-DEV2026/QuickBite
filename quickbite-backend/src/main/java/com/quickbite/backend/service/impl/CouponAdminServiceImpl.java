package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.CouponRequest;
import com.quickbite.backend.dto.response.CouponDetailResponse;
import com.quickbite.backend.entity.Coupon;
import com.quickbite.backend.entity.Notification;
import com.quickbite.backend.enums.AccountStatus;
import com.quickbite.backend.enums.DiscountType;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.enums.Role;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.repository.CouponRepository;
import com.quickbite.backend.repository.NotificationRepository;
import com.quickbite.backend.repository.OrderRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.CouponAdminService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CouponAdminServiceImpl implements CouponAdminService {

    private static final Logger log = LoggerFactory.getLogger(CouponAdminServiceImpl.class);
    // Cancelled/rejected orders do not consume a coupon use (same rule as checkout)
    private static final List<OrderStatus> NOT_COUNTED = List.of(OrderStatus.CANCELLED, OrderStatus.REJECTED);
    private static final Set<String> SORTABLE = Set.of("id", "code", "startDate", "expiryDate");

    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CouponDetailResponse> list(Pageable pageable) {
        Pageable safe = PageUtils.sanitize(pageable, SORTABLE, Sort.by(Sort.Direction.DESC, "id"));
        return couponRepository.findAll(safe).map(this::toResponse);
    }

    @Override
    @Transactional
    public CouponDetailResponse create(CouponRequest request) {
        validate(request);
        String code = request.getCode().trim().toUpperCase();
        if (couponRepository.findByCode(code).isPresent()) {
            throw new BadRequestException("A coupon with code " + code + " already exists");
        }

        Coupon coupon = new Coupon();
        apply(coupon, request, code);
        coupon.setIsActive(request.getIsActive() == null || request.getIsActive());
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon {} created", code);

        if (Boolean.TRUE.equals(request.getNotifyCustomers()) && saved.getIsActive()) {
            notifyCustomers(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CouponDetailResponse update(Long id, CouponRequest request) {
        Coupon coupon = findOrThrow(id);
        validate(request);

        String code = request.getCode().trim().toUpperCase();
        couponRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("A coupon with code " + code + " already exists");
            }
        });

        apply(coupon, request, code);
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }
        return toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponDetailResponse setActive(Long id, boolean active) {
        Coupon coupon = findOrThrow(id);
        coupon.setIsActive(active);
        return toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Coupon coupon = findOrThrow(id);

        // Past orders reference the coupon, so deleting it would break their history
        if (orderRepository.existsByCouponId(id)) {
            coupon.setIsActive(false);
            couponRepository.save(coupon);
            return false;
        }
        couponRepository.delete(coupon);
        return true;
    }

    // ------------------------------------------------------------ helpers

    private Coupon findOrThrow(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }

    private void validate(CouponRequest r) {
        if (!r.getExpiryDate().isAfter(r.getStartDate())) {
            throw new BadRequestException("Expiry date must be after the start date");
        }
        if (r.getDiscountType() == DiscountType.PERCENTAGE
                && r.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("A percentage discount cannot be more than 100%");
        }
    }

    private void apply(Coupon c, CouponRequest r, String code) {
        c.setCode(code);
        c.setDescription(r.getDescription() == null || r.getDescription().isBlank() ? null : r.getDescription().trim());
        c.setDiscountType(r.getDiscountType());
        c.setDiscountValue(r.getDiscountValue());
        c.setMinOrderAmount(r.getMinOrderAmount());
        c.setMaxDiscount(r.getDiscountType() == DiscountType.PERCENTAGE ? r.getMaxDiscount() : null);
        c.setStartDate(r.getStartDate());
        c.setExpiryDate(r.getExpiryDate());
        c.setUsageLimit(r.getUsageLimit());
    }

    // Fine at this scale. For a large user base this would run as a batched background job instead.
    private void notifyCustomers(Coupon c) {
        String message = "New offer: use code " + c.getCode() + " for " + describe(c) + ".";
        List<Notification> batch = userRepository
                .findByRoleAndAccountStatus(Role.CUSTOMER, AccountStatus.ACTIVE).stream()
                .map(u -> Notification.builder().user(u).message(message).type("COUPON").build())
                .toList();
        notificationRepository.saveAll(batch);
        log.info("Coupon {} announced to {} customers", c.getCode(), batch.size());
    }

    private String describe(Coupon c) {
        boolean percentage = c.getDiscountType() == DiscountType.PERCENTAGE;
        String text = percentage ? plain(c.getDiscountValue()) + "% off" : "Rs." + plain(c.getDiscountValue()) + " off";
        if (percentage && c.getMaxDiscount() != null) {
            text += " up to Rs." + plain(c.getMaxDiscount());
        }
        if (c.getMinOrderAmount() != null && c.getMinOrderAmount().signum() > 0) {
            text += " on orders above Rs." + plain(c.getMinOrderAmount());
        }
        return text;
    }

    private String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private CouponDetailResponse toResponse(Coupon c) {
        long used = orderRepository.countByCouponIdAndOrderStatusNotIn(c.getId(), NOT_COUNTED);
        return CouponDetailResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscount(c.getMaxDiscount())
                .startDate(c.getStartDate())
                .expiryDate(c.getExpiryDate())
                .usageLimit(c.getUsageLimit())
                .usedCount(used)
                .isActive(c.getIsActive())
                .status(statusOf(c, used))
                .build();
    }

    private String statusOf(Coupon c, long used) {
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(c.getIsActive())) return "DISABLED";
        if (now.isBefore(c.getStartDate())) return "SCHEDULED";
        if (now.isAfter(c.getExpiryDate())) return "EXPIRED";
        if (used >= c.getUsageLimit()) return "EXHAUSTED";
        return "ACTIVE";
    }
}