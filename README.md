# E-Commerce Backend System

A production-oriented RESTful backend for an E-Commerce application built with **Java 21**, **Spring Boot**, **Spring Security**, **JWT**, **PostgreSQL**, and **Flyway**.

The project focuses on secure authentication, product and stock management, order processing, administration, reviews, database integrity, and automated testing.

---

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven
- Bean Validation
- Lombok
- GreenMail
- H2 for automated tests
- JUnit 5
- MockMvc

---

## Main Features

### Authentication and Security

- User registration
- User login using JWT Bearer tokens
- Stateless authentication
- Role-based authorization
- `USER` and `ADMIN` roles
- Current authenticated user endpoint
- Email verification support
- Password reset flow
- Single-use password reset tokens
- Separate JWT token types for:
  - Access tokens
  - Email verification tokens
  - Password reset tokens
- Generic forgot-password response to prevent email enumeration
- Case-insensitive username and email uniqueness
- Username and email normalization
- Password hashing using BCrypt
- Configurable BCrypt cost
- Trusted frontend CORS configuration
- Consistent `401`, `403`, and validation responses

---

### Product Management

Public users can:

- List active products
- Search products by keyword
- Sort by:
  - ID
  - Name
  - Price
- Use pagination
- Get product details
- View product stock quantity
- View product reviews

Administrators can:

- Create products
- Update products
- Soft-delete products
- Restore inactive products
- View active and inactive products
- Update stock quantity
- View low-stock products
- Filter products by active status

Inactive products cannot be ordered.

---

### Stock Management

- Stock quantity stored separately from product details
- Automatic stock decrease during order creation
- Automatic stock restoration after cancellation
- Atomic stock update queries
- Protection against overselling
- Optimistic locking support
- Database constraint preventing negative stock
- Concurrent order tests

---

### Address Management

Authenticated users can:

- Add addresses
- Update addresses
- View their addresses

Security checks prevent users from accessing or modifying addresses belonging to other users.

Address data is validated before reaching the database.

---

### Order Management

Authenticated users can:

- Create orders
- View their orders
- Filter orders by status
- Use pagination
- View individual order details
- Cancel eligible orders

Administrators can:

- View all orders
- Filter orders by status
- View individual order details
- Update order status

Supported statuses:

- `PENDING`
- `CONFIRMED`
- `CANCELLED`

Valid status transitions are enforced in the service layer.

---

### Order Snapshots

Order history remains accurate even when product or address data changes later.

Each order stores snapshots of:

- Product name
- Product unit price
- Shipping address
- Item totals
- Order total

All monetary values use `BigDecimal`.

---

### Product Reviews

Authenticated users can:

- Create product reviews
- Update their own reviews

Public users can:

- View product reviews with pagination

Review validation includes:

- Rating between 1 and 5
- Required comment
- Comment length limit
- Pagination limits

---

### Admin Dashboard

The admin dashboard summary includes:

- Total products
- Active products
- Inactive products
- Total orders
- Pending orders
- Confirmed orders
- Cancelled orders
- Low-stock products

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Authenticate and receive an access token |
| GET | `/auth/me` | Get the authenticated user |
| POST | `/auth/verify` | Verify an email address |
| POST | `/auth/forgot` | Request a password reset |
| POST | `/auth/reset` | Reset a password |

The forgot-password endpoint returns the same response whether the email exists or not.

---

### Public Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/product` | Search, sort, and paginate active products |
| GET | `/product/{productId}` | Get an active product |
| GET | `/product/{productId}/reviews` | Get product reviews |

Example:

```http
GET /product?keyword=phone&page=0&size=10&sortBy=price&sortDir=desc
```

---

### Reviews

| Method | Endpoint | Description |
|---|---|---|
| POST | `/product/{productId}/review` | Create a product review |
| PUT | `/reviews/{reviewId}` | Update the authenticated user's review |

---

### Addresses

| Method | Endpoint | Description |
|---|---|---|
| GET | `/user/{userId}/address` | Get user addresses |
| POST | `/user/{userId}/address` | Add an address |
| PUT | `/user/{userId}/address/{addressId}` | Update an address |

---

### Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/order` | Create an order |
| GET | `/order` | Get authenticated user's orders |
| GET | `/order/{orderId}` | Get an order |
| PATCH | `/order/{orderId}/cancel` | Cancel an eligible order |

Example create-order request:

```json
{
  "addressId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

---

### Admin Products

All `/admin/**` endpoints require the `ADMIN` role.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/admin/product` | Create a product |
| GET | `/admin/product` | List active and inactive products |
| GET | `/admin/product/{productId}` | Get a product |
| PUT | `/admin/product/{productId}` | Update a product |
| DELETE | `/admin/product/{productId}` | Soft-delete a product |
| PATCH | `/admin/product/{productId}/restore` | Restore a product |
| PATCH | `/admin/product/{productId}/stock` | Update stock quantity |
| GET | `/admin/product/low-stock` | Get low-stock products |

---

### Admin Orders

| Method | Endpoint | Description |
|---|---|---|
| GET | `/admin/order` | List all orders |
| GET | `/admin/order/{orderId}` | Get an order |
| PATCH | `/admin/order/{orderId}/status` | Update order status |

---

### Admin Dashboard

| Method | Endpoint | Description |
|---|---|---|
| GET | `/admin/dashboard/summary` | Get dashboard statistics |

---

## Database Management

The project uses **Flyway** to manage PostgreSQL schema changes.

Hibernate is configured to validate the schema:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway handles:

- Initial database schema
- Order status constraints
- Stock constraints
- Optimistic locking columns
- Product price precision
- Product and shipping snapshots
- Order total snapshots
- Password reset versioning
- User role integrity
- Case-insensitive username and email uniqueness

Never modify an already-applied migration. New database changes must use a new migration version.

---

## Security Highlights

- Stateless JWT authentication
- Bearer token validation
- Explicit token-type validation
- BCrypt password hashing
- Single-use password reset tokens
- Email-enumeration protection
- Case-insensitive unique database indexes
- Role constraints at database level
- Ownership checks for addresses, orders, and reviews
- Soft-deleted products excluded from public ordering
- Trusted-origin CORS policy
- Secure generic authentication errors

---

## Validation and Error Handling

The project uses Bean Validation and a global exception handler.

Example validation response:

```json
{
  "timestamp": "2026-07-14T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "password": "Password must contain at least one letter and one number",
    "username": "Username is required"
  }
}
```

Handled cases include:

- Invalid request fields
- Missing request parameters
- Invalid credentials
- Unverified users
- Invalid or expired tokens
- Missing resources
- Forbidden actions
- Insufficient stock
- Invalid order status transitions
- Invalid product status
- Optimistic locking conflicts

---

## Automated Testing

The project includes tests for:

- Authentication
- Registration validation
- Password policy
- JWT token types
- Single-use password reset tokens
- Email verification
- Forgot-password privacy
- User identifier normalization
- Product validation
- Review validation
- Pagination validation
- Order price snapshots
- Order total snapshots
- Shipping address snapshots
- Stock concurrency
- Product status restrictions
- Security authorization
- CORS configuration
- Service transactions

Run all tests:

```powershell
.\mvnw.cmd clean verify
```

---

## Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/mohameedhendy/E-Commerce-System.git
cd E-Commerce-System
```

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE ecommerce_db;
```

### 3. Configure environment variables

PowerShell example:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/ecommerce_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-database-password"

$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:JWT_ISSUER="eCommerce"
$env:JWT_EXPIRY_SECONDS="3600"

$env:MAIL_HOST="localhost"
$env:MAIL_PORT="1025"
$env:EMAIL_FROM="no-reply@ecommerce.com"

$env:EMAIL_VERIFICATION_ENABLED="false"
$env:FRONTEND_URL="http://localhost:3000"
```

Do not commit real passwords or JWT secrets.

### 4. Run the application

```powershell
.\mvnw.cmd spring-boot:run
```

The API will run at:

```text
http://localhost:8080
```

---

## Authentication Example

Request:

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "UserA",
  "password": "PasswordA123"
}
```

Response:

```json
{
  "token": "JWT_TOKEN"
}
```

Use the token on protected endpoints:

```http
Authorization: Bearer JWT_TOKEN
```

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.ecommerce.ecommerce_backend
│   │       ├── config
│   │       ├── controller
│   │       ├── dao
│   │       ├── dto
│   │       ├── exception
│   │       ├── model
│   │       ├── security
│   │       └── service
│   └── resources
│       ├── db
│       │   └── migration
│       └── application.properties
└── test
    ├── java
    └── resources
```

---

## Next Planned Feature

- Persistent Shopping Cart
- Add products to cart
- Update cart item quantities
- Remove cart items
- Validate stock
- Convert cart into an order
- Clear cart after successful checkout
