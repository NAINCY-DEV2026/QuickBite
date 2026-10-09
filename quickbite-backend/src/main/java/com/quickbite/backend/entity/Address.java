package com.quickbite.backend.entity;

import com.quickbite.backend.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "house_flat")
    private String houseFlat;

    private String street;
    private String area;

    @Column(nullable = false, length = 100)
    private String city;

    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    private String landmark;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    @Builder.Default
    private AddressType addressType = AddressType.HOME;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}