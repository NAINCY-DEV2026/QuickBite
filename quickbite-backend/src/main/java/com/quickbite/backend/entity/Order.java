package com.quickbite.backend.entity;

import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Table is "orders" because ORDER is a reserved SQL keyword
@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Optional — not every order uses a coupon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    // Delivery address is SNAPSHOTTED here, not a foreign key to Address.
    // This way, if the customer edits/deletes the saved address later, past orders stay accurate.
    @Column(name = "delivery_house_flat")
    private String deliveryHouseFlat;
    @Column(name = "delivery_street")
    private String deliveryStreet;
    @Column(name = "delivery_area")
    private String deliveryArea;
    @Column(name = "delivery_city")
    private String deliveryCity;
    @Column(name = "delivery_state")
    private String deliveryState;
    @Column(name = "delivery_pincode")
    private String deliveryPincode;
    @Column(name = "delivery_landmark")
    private String deliveryLandmark;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", nullable = false, precision = 8, scale = 2)
    private BigDecimal deliveryFee;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // OrderItems have no meaning outside the order, so cascade ALL + orphanRemoval is intentional
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}