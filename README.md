# E-Commerce Backend System

A RESTful backend system for an E-Commerce application built with **Java**, **Spring Boot**, **Spring Security**, **JWT**, and **PostgreSQL**.

The project focuses on building clean and practical backend APIs for authentication, products, addresses, and order management.

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Bean Validation
- RESTful APIs
- Global Exception Handling

---

## Features

### Authentication & Security

- User registration
- User login with JWT token
- Protected endpoints using Bearer Token
- Get current authenticated user
- Password reset flow
- Email verification support
- Stateless authentication
- Clean security error handling

---

### Products

- Get all products
- Product pagination
- Product search by keyword
- Product sorting by:
    - id
    - name
    - price
- Get product details by ID
- Clean product response using DTOs
- Product stock quantity included in response

Example:

```http
GET /product?keyword=Product&page=0&size=5&sortBy=price&sortDir=desc
```

---

### Addresses

- Add user address
- Update user address
- Get user addresses
- Prevent users from accessing or modifying addresses that do not belong to them
- Address request/response DTOs
- Validation for address fields

---

### Orders

- Create new order
- Validate selected address belongs to the authenticated user
- Validate product exists
- Validate stock availability before creating an order
- Decrease stock automatically after order creation
- Cancel order
- Restore stock automatically when order is cancelled
- Get all user orders
- Get single order details
- Orders pagination
- Filter orders by status
- Sort orders by newest first
- Order status support:
    - PENDING
    - CONFIRMED
    - CANCELLED
- Order creation timestamp
- Item total and order total calculation using precise decimal values

Example create order request:

```http
POST /order
```

```json
{
  "addressId": 2,
  "items": [
    {
      "productId": 1,
      "quantity": 1
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

Example response:

```json
{
  "id": 6,
  "address": {
    "id": 2,
    "addressLine1": "456 Updated Street",
    "addressLine2": "Floor 2",
    "country": "Egypt",
    "city": "Giza"
  },
  "items": [
    {
      "productId": 1,
      "productName": "Product #1",
      "price": 5.50,
      "quantity": 1,
      "itemTotal": 5.50
    },
    {
      "productId": 2,
      "productName": "Product #2",
      "price": 10.56,
      "quantity": 1,
      "itemTotal": 10.56
    }
  ],
  "orderTotal": 16.06,
  "status": "PENDING",
  "createdAt": "2026-07-01T19:02:00"
}
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login and get JWT token |
| GET | `/auth/me` | Get authenticated user |
| POST | `/auth/forgot` | Request password reset |
| POST | `/auth/reset` | Reset password |
| POST | `/auth/verify` | Verify email |

---

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/product` | Get products with pagination, search, and sorting |
| GET | `/product/{productId}` | Get product details by ID |

---

### Addresses

| Method | Endpoint | Description |
|---|---|---|
| GET | `/user/{userId}/address` | Get user addresses |
| POST | `/user/{userId}/address` | Add new address |
| PUT | `/user/{userId}/address/{addressId}` | Update address |

---

### Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/order` | Create new order |
| GET | `/order` | Get authenticated user orders with pagination and status filter |
| GET | `/order/{orderId}` | Get single order details |
| PATCH | `/order/{orderId}/cancel` | Cancel order and restore stock |

---

## Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/mohameedhendy/E-Commerce-System.git
cd E-Commerce-System
```

---

### 2. Create PostgreSQL database

Create a database named:

```text
ecommerce_db
```

---

### 3. Set environment variables

For Windows PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/ecommerce_db"
$env:DB_USERNAME="ecommerce_user"
$env:DB_PASSWORD="ecommerce123"
$env:JWT_SECRET="SuperSecureSecretKeyForLocalDevelopmentOnly123456789"
```

---

### 4. Run the application

```powershell
.\mvnw.cmd clean spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

---

## Example Login Request

```http
POST /auth/login
```

```json
{
  "username": "mohamed3",
  "password": "Password123"
}
```

Example response:

```json
{
  "token": "JWT_TOKEN_HERE"
}
```

Use the token in protected endpoints:

```text
Authorization: Bearer JWT_TOKEN_HERE
```

---

## Error Handling

The project uses a global exception handler to return clean and consistent API errors.

Example validation error:

```json
{
  "timestamp": "2026-07-01T19:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "size": "Page size must not exceed 50"
  }
}
```

---

## Project Highlights

- Clean DTO-based responses instead of exposing entities directly
- JWT-based stateless authentication
- Product search, sorting, and pagination
- Order creation with stock validation
- Order cancellation with automatic stock restoration
- Secure access checks for user-owned resources
- Global exception handling
- Practical backend flow similar to real E-Commerce systems
- Precise order total calculation using decimal values

---

## Next Planned Features

- Admin product management
- Role-based authorization
- Add product API
- Update product API
- Delete product API
- Admin order status management
- Payment integration simulation
- Unit and integration tests