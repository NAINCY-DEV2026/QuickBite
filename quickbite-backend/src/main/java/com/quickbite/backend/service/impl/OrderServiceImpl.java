package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.OrderRequest;
import com.quickbite.backend.dto.response.OrderPreviewResponse;
import com.quickbite.backend.dto.response.OrderResponse;
import com.quickbite.backend.entity.*;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.enums.PaymentMethod;
import com.quickbite.backend.enums.PaymentStatus;
import com.quickbite.backend.exception.*;
import com.quickbite.backend.mapper.OrderMapper;
import com.quickbite.backend.repository.*;
import com.quickbite.backend.service.CouponService;
import com.quickbite.backend.service.EmailService;
import com.quickbite.backend.service.NotificationService;
import com.quickbite.backend.service.OrderService;
import com.quickbite.backend.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantRepository restaurantRepository;
    private final CouponService couponService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // Single source of truth for order pricing: used by both preview and placeOrder
    private record PriceBreakdown(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal tax,
                                  BigDecimal discount, BigDecimal total, Coupon coupon) {}

    // Valid forward-only transitions. Anything not listed is rejected.
    private Set<OrderStatus> allowedNextStatuses(OrderStatus current) {
        return switch (current) {
            case PLACED -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.REJECTED, OrderStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED);
            case PREPARING -> EnumSet.of(OrderStatus.READY_FOR_PICKUP);
            case READY_FOR_PICKUP -> EnumSet.of(OrderStatus.OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> EnumSet.of(OrderStatus.DELIVERED);
            case DELIVERED, CANCELLED, REJECTED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    // ------------------------------------------------------------ preview

    @Override
    @Transactional(readOnly = true)
    public OrderPreviewResponse previewOrder(Long customerId, String couponCode) {
        Cart cart = loadCheckoutCart(customerId);
        PriceBreakdown price = calculate(cart, couponCode);

        return OrderPreviewResponse.builder()
                .restaurantName(cart.getRestaurant().getName())
                .itemCount(cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum())
                .subtotal(price.subtotal())
                .deliveryFee(price.deliveryFee())
                .tax(price.tax())
                .discount(price.discount())
                .totalAmount(price.total())
                .couponCode(price.coupon() != null ? price.coupon().getCode() : null)
                .build();
    }

    // ------------------------------------------------------------ place order

    @Override
    @Transactional
    public OrderResponse placeOrder(Long customerId, OrderRequest request) {
        PaymentMethod method = parseEnum(PaymentMethod.class, request.getPaymentMethod(), "payment method");

        Cart cart = loadCheckoutCart(customerId);
        Restaurant restaurant = cart.getRestaurant();

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("This address does not belong to you");
        }

        PriceBreakdown price = calculate(cart, request.getCouponCode());

        // COD stays PENDING until cash is collected; simulated card/UPI succeed immediately
        PaymentStatus initialPaymentStatus = method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.SUCCESS;

        Order order = Order.builder()
                .customer(cart.getCustomer())
                .restaurant(restaurant)
                .coupon(price.coupon())
                .deliveryHouseFlat(address.getHouseFlat())
                .deliveryStreet(address.getStreet())
                .deliveryArea(address.getArea())
                .deliveryCity(address.getCity())
                .deliveryState(address.getState())
                .deliveryPincode(address.getPincode())
                .deliveryLandmark(address.getLandmark())
                .subtotal(price.subtotal())
                .deliveryFee(price.deliveryFee())
                .tax(price.tax())
                .discount(price.discount())
                .totalAmount(price.total())
                .orderStatus(OrderStatus.PLACED)
                .paymentStatus(initialPaymentStatus)
                .orderItems(new ArrayList<>())
                .build();

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal itemTotal = cartItem.getFoodItem().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            order.getOrderItems().add(OrderItem.builder()
                    .order(order)
                    .foodItem(cartItem.getFoodItem())
                    .foodNameSnapshot(cartItem.getFoodItem().getName())  // snapshot
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getFoodItem().getPrice())        // snapshot
                    .totalPrice(itemTotal)
                    .build());
        }

        Order saved = orderRepository.save(order);

        paymentRepository.save(Payment.builder()
                .order(saved)
                .amount(price.total())
                .paymentMethod(method)
                .paymentStatus(initialPaymentStatus)
                .transactionRef("SIM-" + System.currentTimeMillis())
                .build());

        cart.getCartItems().clear();
        cart.setRestaurant(null);
        cartRepository.save(cart);

        log.info("Order {} placed by customer {} at restaurant {} (total {}, payment {})",
                saved.getId(), customerId, restaurant.getId(), price.total(), method);

        // In-app notifications (saved in the same transaction as the order)
        notificationService.notify(restaurant.getOwner().getId(),
                "New order #" + saved.getId() + " from " + cart.getCustomer().getName()
                        + " (Rs." + price.total() + ")", "ORDER");
        notificationService.notify(customerId,
                "Your order #" + saved.getId() + " from " + restaurant.getName() + " has been placed.", "ORDER");

        final String customerEmail = cart.getCustomer().getEmail();
        final Long orderId = saved.getId();
        final String totalText = price.total().toPlainString();
        TransactionUtils.afterCommit(() -> emailService.sendOrderConfirmationEmail(customerEmail, orderId, totalText));

        return OrderMapper.toResponse(saved);
    }

    // ------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long requesterId, String requesterRole, Long orderId) {
        Order order = findOrThrow(orderId);

        boolean isAdmin = "ADMIN".equals(requesterRole);
        boolean isCustomer = order.getCustomer().getId().equals(requesterId);
        boolean isRestaurantOwner = order.getRestaurant().getOwner().getId().equals(requesterId);

        if (!isAdmin && !isCustomer && !isRestaurantOwner) {
            throw new UnauthorizedException("You cannot view this order");
        }
        return OrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable).map(OrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getRestaurantOrders(Long ownerId, Long restaurantId, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not manage this restaurant");
        }
        return orderRepository.findByRestaurantId(restaurantId, pageable).map(OrderMapper::toResponse);
    }

    // ------------------------------------------------------------ status changes

    @Override
    @Transactional
    public OrderResponse updateStatus(Long ownerId, Long orderId, String newStatusStr) {
        Order order = findOrThrow(orderId);

        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not manage this restaurant's orders");
        }

        OrderStatus newStatus = parseEnum(OrderStatus.class, newStatusStr, "status");
        OrderStatus current = order.getOrderStatus();

        if (newStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Only the customer can cancel an order. Use REJECTED to decline it.");
        }
        if (!allowedNextStatuses(current).contains(newStatus)) {
            throw new InvalidOrderStateException("Cannot move order from " + current + " to " + newStatus);
        }

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            collectCodPayment(order);   // cash collected at the door
        }
        if (newStatus == OrderStatus.REJECTED) {
            refundIfPaid(order);
        }

        Order saved = orderRepository.save(order);
        log.info("Order {} moved {} -> {} by owner {}", orderId, current, newStatus, ownerId);

        notificationService.notify(order.getCustomer().getId(), statusMessage(newStatus, order), "ORDER");

        if (newStatus == OrderStatus.DELIVERED) {
            final String email = order.getCustomer().getEmail();
            final String restaurantName = order.getRestaurant().getName();
            final Long id = order.getId();
            TransactionUtils.afterCommit(() -> emailService.sendOrderDeliveredEmail(email, id, restaurantName));
        }

        return OrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long customerId, Long orderId) {
        Order order = findOrThrow(orderId);

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("This is not your order");
        }
        if (!allowedNextStatuses(order.getOrderStatus()).contains(OrderStatus.CANCELLED)) {
            throw new InvalidOrderStateException(
                "Order in status " + order.getOrderStatus() + " cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        refundIfPaid(order);
        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled by customer {}", orderId, customerId);

        notificationService.notify(order.getRestaurant().getOwner().getId(),
                "Order #" + order.getId() + " was cancelled by " + order.getCustomer().getName() + ".", "ORDER");
        notificationService.notify(customerId,
                "Your order #" + order.getId() + " has been cancelled.", "ORDER");

        final String email = order.getCustomer().getEmail();
        final Long id = order.getId();
        TransactionUtils.afterCommit(() -> emailService.sendOrderCancellationEmail(email, id));

        return OrderMapper.toResponse(saved);
    }

    // ------------------------------------------------------------ helpers

    // Validates everything a checkout needs: non-empty cart, active restaurant, food still available
    private Cart loadCheckoutCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Your cart is empty"));
        if (cart.getCartItems().isEmpty()) {
            throw new CartException("Your cart is empty");
        }

        Restaurant restaurant = cart.getRestaurant();
        if (restaurant == null || !restaurant.getIsActive()) {
            throw new CartException("This restaurant is not accepting orders right now");
        }

        for (CartItem item : cart.getCartItems()) {
            if (!item.getFoodItem().getIsAvailable()) {
                throw new CartException(item.getFoodItem().getName() + " is no longer available");
            }
        }
        return cart;
    }

    private PriceBreakdown calculate(Cart cart, String couponCode) {
        Restaurant restaurant = cart.getRestaurant();

        BigDecimal subtotal = cart.getCartItems().stream()
                .map(i -> i.getFoodItem().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            coupon = couponService.validateAndGet(couponCode, subtotal);
            discount = couponService.calculateDiscount(coupon, subtotal);
        }

        BigDecimal tax = subtotal.multiply(restaurant.getTaxPercentage())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = restaurant.getDeliveryFee();
        BigDecimal total = subtotal.add(tax).add(deliveryFee).subtract(discount);

        return new PriceBreakdown(subtotal, deliveryFee, tax, discount, total, coupon);
    }

    private String statusMessage(OrderStatus status, Order order) {
        String restaurant = order.getRestaurant().getName();
        Long id = order.getId();
        return switch (status) {
            case CONFIRMED -> restaurant + " confirmed your order #" + id + ".";
            case PREPARING -> restaurant + " is preparing your order #" + id + ".";
            case READY_FOR_PICKUP -> "Your order #" + id + " is ready and waiting for pickup by the delivery partner.";
            case OUT_FOR_DELIVERY -> "Your order #" + id + " is out for delivery.";
            case DELIVERED -> "Your order #" + id + " was delivered. Enjoy your meal! You can rate it from your Orders page.";
            case REJECTED -> restaurant + " could not accept order #" + id + ". Any payment will be refunded.";
            default -> "Your order #" + id + " is now " + status.name().replace('_', ' ').toLowerCase() + ".";
        };
    }

    private Order findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void refundIfPaid(Order order) {
        paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                log.info("Payment for order {} refunded", order.getId());
            }
        });
    }

    private void collectCodPayment(Order order) {
        paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                order.setPaymentStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
            }
        });
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid " + field + ": " + value);
        }
    }
}