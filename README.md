# QuickBite – Full-Stack Food Ordering & Delivery Platform

QuickBite is a Swiggy/Zomato-style food ordering platform with three separate experiences: **customers** who order food, **restaurant owners** who manage menus and incoming orders, and **admins** who moderate the platform and track revenue.

The backend is a layered Spring Boot REST API secured with JWT. The frontend is a separate React single-page application. Pricing, validation, and business rules are enforced on the server.

## Features

### Customers

- Browse restaurants with search, city and cuisine filters, minimum rating, open-now filtering, sorting, and pagination.
- Search dishes across restaurants by name, category, price range, and vegetarian preference.
- Cart that holds items from one restaurant at a time, enforced by the backend.
- Checkout with saved addresses, coupons, and a price breakdown covering subtotal, delivery fee, tax, and discount.
- Simulated Cash on Delivery (COD), UPI, and card payments.
- Order tracking with a status timeline and cancellation of eligible orders.
- Automatic refunds of simulated online payments when an order is cancelled or rejected.
- Reviews and ratings, allowed only for delivered orders and only once per order.
- In-app notifications and email notifications (sent when SMTP credentials are configured).
- Profile photo, editable profile details, password change, and account deactivation.

### Restaurant Owners

- Manage multiple restaurants, each with its own delivery fee, tax rate, and opening hours.
- Add, edit, delete, and toggle the availability of menu items, and upload restaurant and food photos.
- Dishes that appear in past orders are hidden instead of deleted, preserving order history.
- View incoming orders with customer name, phone number, and delivery address.
- Accept, reject, and move orders through the status flow, and mark COD cash as collected on delivery.

### Admins

- Dashboard statistics and reports: revenue per day, top restaurants, most-ordered dishes, and orders by status.
- Block or unblock users and restaurants, and view all platform orders.
- Create, edit, enable, disable, and delete coupons, with an optional announcement to customers.
- Manage food categories.

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java, Spring Boot, Spring Web (MVC), Spring Data JPA, Hibernate, Spring Security, JWT, Jakarta Bean Validation, JavaMail, Maven |
| Database | MySQL 8 |
| Frontend | React, Vite, React Router, Axios, Bootstrap, Context API |
| Tools | STS / Eclipse, Visual Studio Code, MySQL Workbench, Postman, Git, GitHub |

## Architecture

```text
React + Bootstrap
       |
       | Axios (Authorization: Bearer <JWT>)
       v
REST Controllers
       |
       v
Services (business logic, transactions)
       |
       v
Spring Data JPA Repositories
       |
       v
MySQL

Spring Security + JWT filter -> authentication and authorization on every request
Supporting services           -> notifications, email, file storage
```

Repository layout:

```text
QuickBite/
├── quickbite-backend/     Spring Boot REST API
├── quickbite-frontend/    React (Vite) application
└── README.md
```

Backend packages:

```text
com.quickbite.backend
├── controller
├── service
├── service.impl
├── repository
├── entity
├── dto.request
├── dto.response
├── mapper
├── security
├── config
├── exception
└── util
```

## Key Design Decisions

- **DTO-based responses:** Entities are never returned from controllers. This avoids exposing internal fields and infinite JSON recursion from bidirectional relationships.
- **Order snapshots:** Dish names, prices, and the delivery address are stored with each order, so history stays correct even when a restaurant changes a price or a customer edits an address.
- **One pricing code path:** Checkout preview and order placement use the same calculation, so the total shown to the customer matches the order that is created.
- **Order status validation:** Only valid forward transitions are accepted (for example, `DELIVERED` can never go back to `PREPARING`). Cancelling or rejecting an order refunds a simulated online payment, and delivering a COD order marks the payment as collected.
- **Layered authorization:** Role rules in Spring Security and `@PreAuthorize` are combined with ownership checks inside the services, so users can only access authorized orders, payments, and restaurants.
- **Immediate account status checks:** The JWT filter loads the user on every request, so blocking or deactivating an account takes effect on the next call.
- **Side effects after commit:** Emails are sent asynchronously and only after the database transaction commits, so a rolled-back order does not send a confirmation.
- **Soft deletion:** Restaurants, dishes used in orders, and coupons used in orders are deactivated instead of deleted, preserving historical records.
- **Pluggable file storage:** Uploads go through a `FileStorageService` interface backed by local disk, allowing cloud storage to be added without changing business logic. Only JPG, PNG, WEBP, and GIF files up to 5 MB are accepted.
- **Safer search queries:** Search endpoints use JPQL with optional filters, whitelisted sort fields, and a capped page size.

## Database

The application uses 14 tables:

`user`, `restaurant`, `category`, `food_item`, `cart`, `cart_item`, `address`, `orders`, `order_item`, `payment`, `coupon`, `review`, `notification`, `password_reset_token`.

Hibernate creates and updates the schema from entity classes using `spring.jpa.hibernate.ddl-auto=update`, which is convenient during development. For production, a migration tool such as Flyway is recommended.

## API Overview

**Base URL:** `http://localhost:8090/api`

| Area | Endpoints |
|---|---|
| Authentication | `POST /auth/register`, `/auth/register-owner`, `/auth/login`, `/auth/forgot-password`, `/auth/reset-password` |
| Users | `GET/PUT /users/me`, `PUT /users/change-password`, `POST /users/profile-image`, `PUT /users/deactivate` |
| Restaurants | `GET /restaurants`, `GET /restaurants/{id}`, `GET /restaurants/mine`, `POST /restaurants`, `PUT/DELETE /restaurants/{id}`, `POST /restaurants/{id}/image` |
| Dishes | `GET /foods`, `GET/POST /restaurants/{id}/foods`, `PUT/DELETE /foods/{id}`, `PATCH /foods/{id}/availability`, `POST /foods/{id}/image` |
| Cart | `GET /cart`, `POST /cart/items`, `PUT /cart/items/{id}?quantity=`, `DELETE /cart/items/{id}`, `DELETE /cart` |
| Orders | `POST /orders/preview`, `POST /orders`, `GET /orders`, `GET /orders/{id}`, `PUT /orders/{id}/cancel` |
| Owner orders | `GET /restaurant/{restaurantId}/orders`, `PUT /restaurant/orders/{id}/status` |
| Payments | `GET /payments/{orderId}`, `PUT /payments/{orderId}/mark-paid`, `PUT /payments/{orderId}/mark-failed` |
| Coupons | `POST /coupons/validate` |
| Addresses | `GET/POST /addresses`, `PUT/DELETE /addresses/{id}` |
| Reviews | `POST /reviews`, `GET /restaurants/{id}/reviews` |
| Notifications | `GET /notifications`, `GET /notifications/unread-count`, `PUT /notifications/{id}/read`, `PUT /notifications/read-all` |
| Admin | `GET /admin/stats`, `/admin/reports?days=`, `/admin/users`, `/admin/restaurants`, `/admin/orders`, `/admin/coupons`, `/admin/categories` |

Search endpoints support filters such as `city`, `name`, `cuisine`, `minRating`, `openNow`, `category`, `minPrice`, `maxPrice`, and `vegetarian`, along with `page`, `size`, and `sort`.

Errors use a consistent JSON structure and do not expose stack traces:

```json
{
  "timestamp": "2026-10-09T12:00:00",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/auth/register",
  "fieldErrors": {
    "phone": "Enter a valid 10-digit mobile number"
  }
}
```

## Getting Started

### Prerequisites

- JDK 17 or newer
- Node.js 18 or newer and npm
- MySQL 8
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/NAINCY-DEV2026/QuickBite.git
cd QuickBite
```

### 2. Create the Database

Open MySQL Workbench and run:

```sql
CREATE DATABASE quickbite_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 3. Configure and Run the Backend

Open the `quickbite-backend` directory.

Configure the required environment variables listed in the Configuration section. Keep your local `.env` file private and do not commit it to GitHub.

Run the backend from the `quickbite-backend` directory:

```bash
mvn spring-boot:run
```

On Windows, if the Maven Wrapper is present, you can use:

```bat
mvnw.cmd spring-boot:run
```

If you run the project from STS or Eclipse, set the run configuration's working directory to the `quickbite-backend` folder so the local environment configuration is found.

The API runs at `http://localhost:8090`.

### 4. Run the Frontend

Open a terminal in the `quickbite-frontend` directory:

```bash
npm install
npm run dev
```

Open `http://localhost:5173`.

The frontend API base URL is `http://localhost:8090/api` by default. If your frontend is configured to use `VITE_API_URL`, create `quickbite-frontend/.env` and set:

```env
VITE_API_URL=http://localhost:8090/api
```

## Demo Accounts

The following development demo accounts are available when demo data has been initialized successfully.

| Role | Email | Password |
|---|---|---|
| Customer | `customer1@quickbite.com` to `customer5@quickbite.com` | `customer123` |
| Restaurant owner | `owner1@quickbite.com` to `owner3@quickbite.com` | `owner123` |

The demo data contains 10 restaurants with menus, delivered orders with reviews, and two coupons:

- **QUICK20:** 20% off, up to ₹100, on orders above ₹500.
- **WELCOME50:** ₹50 off on orders above ₹200.

### Creating an Admin Account

Register a normal account first. Then, in your development database, update its role:

```sql
UPDATE `user`
SET role = 'ADMIN'
WHERE email = 'you@example.com';
```

Replace `you@example.com` with your registered email address, then log in again to receive a token with the updated role.

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `DB_PASSWORD` | MySQL password | Required |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_NAME` | Database name | `quickbite_db` |
| `JWT_SECRET` | JWT signing key, at least 32 characters | Required |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds | `86400000` (24 hours) |
| `MAIL_USERNAME` | Email account used to send mail | Empty |
| `MAIL_PASSWORD` | Email password or app password | Empty |
| `FRONTEND_URL` | Frontend URL used in password-reset links | `http://localhost:5173` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:5173,http://localhost:3000` |
| `VITE_API_URL` | Frontend backend API base URL | `http://localhost:8090/api` |

Secrets should be supplied through environment variables or a local, Git-ignored `.env` file. Never commit database passwords, JWT keys, email app passwords, or other private credentials.

## Security Notes

- Passwords are hashed with BCrypt and are never returned by the API.
- Authentication uses stateless JWTs, and CORS origins are configurable.
- Uploaded files are limited to image types and saved under random file names.
- The demo data seeder is intended for development. Disable it before exposing the application publicly.
- Payments are simulated; the application is not connected to a real payment gateway.

## Roadmap

- Automated unit and integration tests
- Docker setup and a hosted demo
- Cloud image storage through the existing storage interface
- Real payment gateway integration
- Live order updates over WebSockets instead of polling
- Refresh tokens and login rate limiting
- Database migrations with Flyway

## Author

**Naincy Gupta**

- **GitHub:** [NAINCY-DEV2026](https://github.com/NAINCY-DEV2026)
- **LinkedIn:** [Naincy Gupta](https://www.linkedin.com/in/naincy-gupta-1568572b5/)

---

*Built with Java, Spring Boot, React, and MySQL.*
