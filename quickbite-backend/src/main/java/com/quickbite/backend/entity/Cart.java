package com.quickbite.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// One cart per customer
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false, unique = true)
	private User customer;

	// Nullable until the customer adds the first item
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "restaurant_id")
	private Restaurant restaurant;

	// Cart items have no meaning outside the cart, so cascade ALL + orphanRemoval
	// is intentional here
	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CartItem> cartItems = new ArrayList<>();
}