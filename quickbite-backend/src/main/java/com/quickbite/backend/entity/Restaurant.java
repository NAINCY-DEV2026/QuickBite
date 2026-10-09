package com.quickbite.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "restaurant")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many restaurants can belong to one owner (owner can have multiple restaurants)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    private String phone;
    private String email;

    @Column(name = "cuisine_type")
    private String cuisineType;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "delivery_fee", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    private String image;

    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}