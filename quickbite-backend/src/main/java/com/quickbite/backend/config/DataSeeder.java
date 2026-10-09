package com.quickbite.backend.config;

import com.quickbite.backend.entity.*;
import com.quickbite.backend.enums.*;
import com.quickbite.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, Category> categories = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        // Idempotent: if seed data already exists, do nothing
        if (userRepository.existsByEmail("owner1@quickbite.com")) {
            log.info("Seed data already present - skipping");
            return;
        }
        log.info("Seeding QuickBite demo data...");

        for (String name : List.of("Biryani", "Indian", "Pizza", "Italian", "Burger", "Chinese",
                "Desserts", "Beverages", "Snacks", "South Indian", "Healthy")) {
            categories.put(name, categoryRepository.findByName(name)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build())));
        }

        User owner1 = user("Rajesh Kumar", "owner1@quickbite.com", "9810000001", "owner123", Role.RESTAURANT_OWNER);
        User owner2 = user("Meera Nair", "owner2@quickbite.com", "9810000002", "owner123", Role.RESTAURANT_OWNER);
        User owner3 = user("Sandeep Rao", "owner3@quickbite.com", "9810000003", "owner123", Role.RESTAURANT_OWNER);

        List<User> customers = List.of(
                user("Aarav Sharma", "customer1@quickbite.com", "9820000001", "customer123", Role.CUSTOMER),
                user("Priya Verma", "customer2@quickbite.com", "9820000002", "customer123", Role.CUSTOMER),
                user("Rohan Gupta", "customer3@quickbite.com", "9820000003", "customer123", Role.CUSTOMER),
                user("Sneha Iyer", "customer4@quickbite.com", "9820000004", "customer123", Role.CUSTOMER),
                user("Karan Mehta", "customer5@quickbite.com", "9820000005", "customer123", Role.CUSTOMER));

        // ---------- 1. Spice Route Biryani House ----------
        Restaurant r1 = restaurant(owner1, "Spice Route Biryani House", "Slow-cooked dum biryani and Mughlai classics.",
                "Connaught Place", "Delhi", "Biryani, Indian", 11, 23, 30, 5);
        seedReviews(r1, List.of(
                food(r1, "Biryani", "Hyderabadi Chicken Dum Biryani", "Slow-cooked basmati layered with spiced chicken", 289, false),
                food(r1, "Biryani", "Mutton Biryani", "Tender mutton, saffron rice, boiled egg", 349, false),
                food(r1, "Biryani", "Veg Dum Biryani", "Seasonal vegetables, mint and fried onions", 219, true),
                food(r1, "Biryani", "Paneer Tikka Biryani", "Smoky paneer tikka over fragrant rice", 249, true),
                food(r1, "Snacks", "Chicken 65", "Crispy curry-leaf fried chicken", 219, false),
                food(r1, "Indian", "Mirchi Ka Salan", "Tangy peanut-sesame chilli curry", 99, true),
                food(r1, "Desserts", "Double Ka Meetha", "Hyderabadi bread pudding with saffron milk", 129, true),
                food(r1, "Beverages", "Masala Chaas", "Spiced buttermilk, served chilled", 59, true)),
                customers, new int[]{5, 5, 4, 4, 5},
                new String[]{"Best biryani I've had in Delhi. The rice was perfectly layered.",
                        "Hot and on time. The chicken 65 was fantastic.",
                        "Great flavour, portion could be a bit bigger.",
                        "Mutton was tender, loved the salan on the side.",
                        "Ordered three times this week already!"});

        // ---------- 2. Punjab Da Dhaba ----------
        Restaurant r2 = restaurant(owner1, "Punjab Da Dhaba", "Butter-heavy, tandoor-fresh North Indian comfort food.",
                "Sector 18", "Noida", "North Indian", 10, 23, 40, 5);
        seedReviews(r2, List.of(
                food(r2, "Indian", "Dal Makhani", "Overnight-simmered black lentils with cream", 229, true),
                food(r2, "Indian", "Butter Chicken", "Classic tomato-butter gravy with tandoori chicken", 329, false),
                food(r2, "Indian", "Paneer Butter Masala", "Cottage cheese in a rich cashew gravy", 279, true),
                food(r2, "Indian", "Garlic Naan", "Tandoor-baked, brushed with garlic butter", 59, true),
                food(r2, "Indian", "Amritsari Kulcha", "Stuffed kulcha with chole and chutney", 99, true),
                food(r2, "Indian", "Chole Bhature", "Spiced chickpeas with two fluffy bhature", 149, true),
                food(r2, "Beverages", "Sweet Lassi", "Thick Punjabi lassi topped with malai", 89, true),
                food(r2, "Desserts", "Gulab Jamun", "Two warm jamuns in cardamom syrup", 99, true)),
                customers, new int[]{5, 4, 5, 4},
                new String[]{"Dal makhani tastes like home. Absolutely creamy.",
                        "Butter chicken was rich, naan slightly cold.",
                        "Chole bhature were huge and delicious.",
                        "Good value for money."});

        // ---------- 3. Tandoori Nights ----------
        Restaurant r3 = restaurant(owner1, "Tandoori Nights", "Kebabs and curries straight from the clay oven.",
                "Hauz Khas Village", "Delhi", "Mughlai, Tandoor", 12, 23, 45, 5);
        seedReviews(r3, List.of(
                food(r3, "Indian", "Tandoori Chicken (Half)", "Yoghurt-marinated, charred in the tandoor", 299, false),
                food(r3, "Snacks", "Seekh Kebab", "Minced lamb kebabs with mint chutney", 279, false),
                food(r3, "Snacks", "Paneer Tikka", "Smoky paneer cubes with peppers", 249, true),
                food(r3, "Snacks", "Hara Bhara Kebab", "Spinach and pea patties", 199, true),
                food(r3, "Indian", "Mutton Rogan Josh", "Kashmiri-style slow-braised mutton", 399, false),
                food(r3, "Indian", "Butter Naan", "Soft and buttery", 49, true),
                food(r3, "Desserts", "Phirni", "Chilled ground-rice pudding in a clay bowl", 119, true),
                food(r3, "Beverages", "Jaljeera", "Cumin and mint cooler", 49, true)),
                customers, new int[]{4, 5, 4, 3, 5},
                new String[]{"Kebabs were juicy and well spiced.",
                        "Tandoori chicken was perfectly charred. Highly recommend.",
                        "Rogan josh was great, a bit oily though.",
                        "Delivery took longer than expected but food was fine.",
                        "Seekh kebab is the best in the area."});

        // ---------- 4. Napoli Pizza Co. ----------
        Restaurant r4 = restaurant(owner2, "Napoli Pizza Co.", "Hand-stretched, wood-fired style pizzas and pasta.",
                "Bandra West", "Mumbai", "Italian, Pizza", 11, 24, 35, 5);
        seedReviews(r4, List.of(
                food(r4, "Pizza", "Margherita", "Tomato, fresh mozzarella and basil", 249, true),
                food(r4, "Pizza", "Farmhouse", "Capsicum, onion, mushroom and olives", 329, true),
                food(r4, "Pizza", "Pepperoni Feast", "Double pepperoni, mozzarella", 399, false),
                food(r4, "Pizza", "BBQ Chicken", "Smoky BBQ sauce and grilled chicken", 379, false),
                food(r4, "Italian", "White Sauce Pasta", "Penne in creamy garlic bechamel", 229, true),
                food(r4, "Snacks", "Garlic Breadsticks", "Served with cheesy dip", 129, true),
                food(r4, "Desserts", "Tiramisu", "Classic espresso-soaked mascarpone cake", 189, true),
                food(r4, "Beverages", "Cold Coffee", "Thick, blended and chilled", 119, true)),
                customers, new int[]{5, 4, 4, 5, 3},
                new String[]{"Crust was thin and crispy. Proper pizza!",
                        "Farmhouse was loaded with veggies.",
                        "Pasta was creamy, loved it.",
                        "Pepperoni feast is a must-try.",
                        "Pizza arrived a little lukewarm."});

        // ---------- 5. Burger Barn ----------
        Restaurant r5 = restaurant(owner2, "Burger Barn", "Smash burgers, loaded fries and thick shakes.",
                "Saket", "Delhi", "Fast Food, Burgers", 11, 24, 30, 5);
        seedReviews(r5, List.of(
                food(r5, "Burger", "Classic Smash Burger", "Double smashed patty, cheese, house sauce", 199, false),
                food(r5, "Burger", "Crispy Veg Burger", "Crunchy veggie patty with slaw", 149, true),
                food(r5, "Burger", "Double Cheese Chicken Burger", "Crispy chicken, two cheese slices", 259, false),
                food(r5, "Burger", "Paneer Zinger", "Spicy crumb-fried paneer", 189, true),
                food(r5, "Snacks", "Peri Peri Fries", "Crisp fries with peri peri seasoning", 99, true),
                food(r5, "Snacks", "Loaded Nachos", "Cheese, salsa, jalapenos", 159, true),
                food(r5, "Beverages", "Chocolate Shake", "Thick Belgian chocolate shake", 149, true),
                food(r5, "Beverages", "Oreo Thick Shake", "Crushed Oreo and vanilla ice cream", 169, true)),
                customers, new int[]{5, 4, 5, 4, 4},
                new String[]{"The smash burger is unreal. Juicy and cheesy.",
                        "Fries were crispy even after delivery.",
                        "Best shakes in the neighbourhood.",
                        "Paneer zinger had a nice kick.",
                        "Good burgers, packaging could be better."});

        // ---------- 6. Dragon Wok ----------
        Restaurant r6 = restaurant(owner2, "Dragon Wok", "Indo-Chinese street favourites, wok-tossed to order.",
                "Indiranagar", "Bangalore", "Chinese", 12, 23, 35, 5);
        seedReviews(r6, List.of(
                food(r6, "Chinese", "Veg Hakka Noodles", "Wok-tossed noodles with crunchy veggies", 179, true),
                food(r6, "Chinese", "Chicken Hakka Noodles", "Noodles with shredded chicken and egg", 219, false),
                food(r6, "Chinese", "Schezwan Fried Rice", "Fiery schezwan sauce, spring onion", 199, true),
                food(r6, "Chinese", "Chilli Chicken (Dry)", "Crisp chicken with peppers in chilli sauce", 259, false),
                food(r6, "Chinese", "Veg Manchurian Gravy", "Vegetable dumplings in garlic gravy", 189, true),
                food(r6, "Chinese", "Hot & Sour Soup", "Peppery and tangy", 119, true),
                food(r6, "Snacks", "Honey Chilli Potato", "Crispy potato in sweet-spicy glaze", 169, true),
                food(r6, "Desserts", "Fried Ice Cream", "Crunchy outside, cold inside", 149, true)),
                customers, new int[]{4, 5, 4, 5},
                new String[]{"Hakka noodles had proper wok flavour.",
                        "Chilli chicken was spicy and crisp. Loved it.",
                        "Manchurian gravy was a bit sweet for me.",
                        "Honey chilli potato is addictive."});

        // ---------- 7. Sweet Tooth Desserts ----------
        Restaurant r7 = restaurant(owner3, "Sweet Tooth Desserts", "Cakes, brownies and Indian sweets for every craving.",
                "Sector 62", "Noida", "Desserts", 10, 23, 25, 5);
        seedReviews(r7, List.of(
                food(r7, "Desserts", "Belgian Chocolate Brownie", "Fudgy brownie with dark chocolate chunks", 129, true),
                food(r7, "Desserts", "Red Velvet Pastry", "Cream cheese frosting, soft sponge", 99, true),
                food(r7, "Desserts", "Rasmalai Tub", "Soft paneer discs in saffron milk", 149, true),
                food(r7, "Desserts", "Mango Cheesecake Jar", "No-bake cheesecake with mango compote", 179, true),
                food(r7, "Desserts", "Waffle with Nutella", "Crisp waffle, warm Nutella, banana", 199, true),
                food(r7, "Desserts", "Gajar Halwa", "Slow-cooked carrot halwa with khoya", 119, true),
                food(r7, "Desserts", "Choco Lava Cake", "Molten centre, served warm", 109, true),
                food(r7, "Beverages", "Hot Chocolate", "Rich and velvety", 129, true)),
                customers, new int[]{5, 5, 4, 5, 4},
                new String[]{"The brownie is dangerously good.",
                        "Choco lava cake was perfectly molten.",
                        "Rasmalai was fresh and not too sweet.",
                        "Waffle with Nutella made my evening.",
                        "Cheesecake jar was lovely, slightly small."});

        // ---------- 8. Dosa Junction ----------
        Restaurant r8 = restaurant(owner3, "Dosa Junction", "Crisp dosas, fluffy idlis and filter coffee.",
                "Koramangala", "Bangalore", "South Indian", 7, 22, 25, 5);
        seedReviews(r8, List.of(
                food(r8, "South Indian", "Masala Dosa", "Crisp dosa with spiced potato filling", 129, true),
                food(r8, "South Indian", "Mysore Masala Dosa", "Red chutney spread, extra spicy", 149, true),
                food(r8, "South Indian", "Idli Sambar (3 pcs)", "Soft idlis with sambar and chutneys", 99, true),
                food(r8, "South Indian", "Medu Vada", "Crisp lentil donuts", 99, true),
                food(r8, "South Indian", "Rava Onion Uttapam", "Thick semolina pancake with onions", 139, true),
                food(r8, "South Indian", "Ven Pongal", "Peppery rice and lentil comfort bowl", 119, true),
                food(r8, "Beverages", "Filter Coffee", "Strong South Indian decoction coffee", 59, true),
                food(r8, "Desserts", "Payasam", "Vermicelli pudding with cashews", 89, true)),
                customers, new int[]{5, 5, 4, 5},
                new String[]{"Dosa was crisp and chutneys were fresh.",
                        "Filter coffee is the real deal.",
                        "Idlis were soft, sambar a little thin.",
                        "Mysore masala dosa is my go-to now."});

        // ---------- 9. The Chai Corner ----------
        Restaurant r9 = restaurant(owner3, "The Chai Corner", "Cutting chai and street snacks, the way they should be.",
                "Karol Bagh", "Delhi", "Snacks, Beverages", 8, 23, 20, 5);
        seedReviews(r9, List.of(
                food(r9, "Beverages", "Masala Chai", "Strong, spiced milk tea", 39, true),
                food(r9, "Beverages", "Ginger Cardamom Chai", "Fresh ginger and crushed cardamom", 49, true),
                food(r9, "Beverages", "Iced Lemon Tea", "Chilled and tangy", 89, true),
                food(r9, "Snacks", "Vada Pav", "Mumbai-style potato fritter in a pav", 49, true),
                food(r9, "Snacks", "Samosa (2 pcs)", "Crisp pastry, spiced potato filling", 49, true),
                food(r9, "Snacks", "Bun Maska", "Soft bun with butter, dunk in chai", 59, true),
                food(r9, "Snacks", "Aloo Tikki Chaat", "Crispy tikki, curd and chutneys", 89, true),
                food(r9, "Snacks", "Masala Maggi", "Everyone's late-night favourite", 79, true)),
                customers, new int[]{5, 4, 5, 4, 5},
                new String[]{"Chai was perfect for a rainy day.",
                        "Samosas were hot and crisp.",
                        "Bun maska + chai is the best combo.",
                        "Aloo tikki chaat was tangy and fresh.",
                        "Great snacks at a great price."});

        // ---------- 10. Green Bowl Kitchen ----------
        Restaurant r10 = restaurant(owner3, "Green Bowl Kitchen", "Fresh bowls, wraps and smoothies for a lighter meal.",
                "Powai", "Mumbai", "Healthy, Salads", 9, 22, 40, 5);
        seedReviews(r10, List.of(
                food(r10, "Healthy", "Grilled Chicken Salad Bowl", "Greens, grilled chicken, lemon-olive dressing", 299, false),
                food(r10, "Healthy", "Quinoa Veggie Bowl", "Quinoa, roasted veg, tahini drizzle", 279, true),
                food(r10, "Healthy", "Paneer Tikka Wrap", "Whole-wheat wrap with mint yoghurt", 199, true),
                food(r10, "Healthy", "Hummus & Pita Platter", "House hummus with warm pita", 189, true),
                food(r10, "Healthy", "Avocado Toast", "Sourdough, avocado, chilli flakes", 229, true),
                food(r10, "Healthy", "Egg White Omelette", "Spinach and mushroom filling", 159, false),
                food(r10, "Beverages", "Green Detox Smoothie", "Spinach, apple, cucumber, mint", 149, true),
                food(r10, "Desserts", "Fruit Yogurt Parfait", "Greek yoghurt, granola, seasonal fruit", 139, true)),
                customers, new int[]{4, 5, 4, 5},
                new String[]{"Fresh ingredients and generous portions.",
                        "Quinoa bowl kept me full for hours.",
                        "Wrap was tasty, wish it had more filling.",
                        "The detox smoothie is excellent."});

        // ---------- Coupons ----------
     // ---------- Coupons ----------
        coupon("QUICK20", "20% off up to Rs.100 on orders above Rs.500", DiscountType.PERCENTAGE, 20, 500, 100.0);
        coupon("WELCOME50", "Flat Rs.50 off on orders above Rs.200", DiscountType.FIXED, 50, 200, null);

        log.info("Seed data created: 10 restaurants, menus, reviews and coupons.");
    }

    // ------------------------------------------------------------------ helpers

    private User user(String name, String email, String phone, String rawPassword, Role role) {
        return userRepository.save(User.builder()
                .name(name).email(email).phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(role).build());
    }

    private Restaurant restaurant(User owner, String name, String desc, String area, String city,
                                  String cuisine, int openHour, int closeHour, double fee, double taxPct) {
        return restaurantRepository.save(Restaurant.builder()
                .owner(owner).name(name).description(desc)
                .address(area).city(city).cuisineType(cuisine)
                .phone("9800000000").email(name.toLowerCase().replaceAll("[^a-z]", "") + "@quickbite.com")
                .openingTime(LocalTime.of(openHour, 0))
                .closingTime(LocalTime.of(closeHour % 24, 0))
                .deliveryFee(BigDecimal.valueOf(fee).setScale(2))
                .taxPercentage(BigDecimal.valueOf(taxPct).setScale(2))
                .build());
    }

    private FoodItem food(Restaurant r, String category, String name, String desc, double price, boolean veg) {
        return foodItemRepository.save(FoodItem.builder()
                .restaurant(r).category(categories.get(category))
                .name(name).description(desc)
                .price(BigDecimal.valueOf(price).setScale(2))
                .isVeg(veg).isAvailable(true).build());
    }

    private void coupon(String code, String desc, DiscountType type, double value, double minOrder, Double maxDiscount) {
        if (couponRepository.findByCode(code).isPresent()) return;
        couponRepository.save(Coupon.builder()
                .code(code).description(desc).discountType(type)
                .discountValue(BigDecimal.valueOf(value))
                .minOrderAmount(BigDecimal.valueOf(minOrder))
                .maxDiscount(maxDiscount == null ? null : BigDecimal.valueOf(maxDiscount))
                .startDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusYears(1))
                .usageLimit(1000).build());
    }

    // Creates one DELIVERED order + payment + review per rating, then recomputes the restaurant's average rating
    private void seedReviews(Restaurant r, List<FoodItem> foods, List<User> customers, int[] ratings, String[] comments) {
        int sum = 0;
        for (int i = 0; i < ratings.length; i++) {
            User customer = customers.get(i % customers.size());
            FoodItem f1 = foods.get(i % foods.size());
            FoodItem f2 = foods.get((i + 2) % foods.size());
            int qty1 = 1 + (i % 2);

            BigDecimal line1 = f1.getPrice().multiply(BigDecimal.valueOf(qty1));
            BigDecimal line2 = f2.getPrice();
            BigDecimal subtotal = line1.add(line2);
            BigDecimal tax = subtotal.multiply(r.getTaxPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(tax).add(r.getDeliveryFee());

            Order order = Order.builder()
                    .customer(customer).restaurant(r)
                    .deliveryHouseFlat("B-" + (101 + i)).deliveryStreet("Main Road")
                    .deliveryArea("Central").deliveryCity(r.getCity())
                    .deliveryState("India").deliveryPincode("110001").deliveryLandmark("Near Metro Station")
                    .subtotal(subtotal).deliveryFee(r.getDeliveryFee()).tax(tax)
                    .discount(BigDecimal.ZERO).totalAmount(total)
                    .orderStatus(OrderStatus.DELIVERED).paymentStatus(PaymentStatus.SUCCESS)
                    .orderItems(new ArrayList<>())
                    .build();

            order.getOrderItems().add(OrderItem.builder().order(order).foodItem(f1)
                    .foodNameSnapshot(f1.getName()).quantity(qty1)
                    .unitPrice(f1.getPrice()).totalPrice(line1).build());
            order.getOrderItems().add(OrderItem.builder().order(order).foodItem(f2)
                    .foodNameSnapshot(f2.getName()).quantity(1)
                    .unitPrice(f2.getPrice()).totalPrice(line2).build());

            Order saved = orderRepository.save(order);

            paymentRepository.save(Payment.builder()
                    .order(saved).amount(total)
                    .paymentMethod(i % 2 == 0 ? PaymentMethod.UPI_SIMULATION : PaymentMethod.COD)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .transactionRef("SEED-" + saved.getId()).build());

            reviewRepository.save(Review.builder()
                    .customer(customer).restaurant(r).order(saved)
                    .rating(ratings[i]).comment(comments[i]).build());

            sum += ratings[i];
        }
        r.setRating(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(ratings.length), 2, RoundingMode.HALF_UP));
        restaurantRepository.save(r);
    }
}