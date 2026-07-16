# E-Commerce Backend System

A production-oriented REST API for an e-commerce platform built with **Java 21**, **Spring Boot**, **Spring Security**, **JWT**, **PostgreSQL**, and **Flyway**.

The project demonstrates secure authentication, refresh-token session management, persistent shopping carts, product and inventory management, order processing, administration, database migrations, OpenAPI documentation, containerization, and automated CI testing.

---

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven Wrapper
- Bean Validation
- Lombok
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger UI
- Docker and Docker Compose
- GitHub Actions
- GitHub Container Registry
- GreenMail
- H2 for automated tests
- JUnit 5
- Mockito
- MockMvc

---

## Main Features

### Authentication and Security

- User registration and login
- JWT Bearer access tokens
- Refresh-token authentication flow
- Persistent authentication sessions
- Refresh-token rotation
- Logout from the current session
- Logout from all sessions
- List active sessions
- Revoke a specific session
- Stateless Spring Security configuration
- Role-based authorization using `USER` and `ADMIN`
- Current authenticated user endpoint
- Email verification support
- Password reset flow
- Single-use password reset tokens
- Separate token types for:
  - Access tokens
  - Email verification tokens
  - Password reset tokens
- Generic forgot-password responses to prevent email enumeration
- Case-insensitive username and email uniqueness
- Username and email normalization
- BCrypt password hashing
- Configurable BCrypt cost
- Trusted frontend CORS configuration
- Consistent JSON responses for validation and security errors
- Security headers and Content Security Policy
- Swagger UI security policy separated from the API policy

---

### Product and Inventory Management

Public users can:

- List active products
- Search products by keyword
- Sort products by ID, name, or price
- Use pagination
- Get product details
- View available stock
- View product reviews

Administrators can:

- Create products
- Update products
- Soft-delete products
- Restore inactive products
- View active and inactive products
- Filter products by active status
- Update stock quantity
- View low-stock products

Inventory protection includes:

- Atomic stock updates
- Automatic stock decrease during ordering
- Automatic stock restoration after cancellation
- Protection against overselling
- Optimistic locking
- A database constraint preventing negative stock
- Concurrent order tests

Inactive products cannot be ordered.

---

### Persistent Shopping Cart

Authenticated users can:

- Get their shopping cart
- Add products to the cart
- Update cart item quantities
- Remove individual cart items
- Clear the cart
- Validate product activity and available stock
- Checkout the cart into an order
- Automatically clear the cart after successful checkout

Cart totals are calculated from current product prices and validated before checkout.

---

### Address Management

Authenticated users can:

- Add addresses
- Update addresses
- View their addresses

Ownership checks prevent users from accessing or modifying addresses that belong to other users.

Address data is validated before reaching the database.

---

### Order Management

Authenticated users can:

- Create orders directly
- Create orders through cart checkout
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

- Create reviews for eligible products
- Update their own reviews

Public users can:

- View product reviews with pagination

Validation includes:

- Rating between 1 and 5
- Required comment
- Comment length limits
- Pagination limits
- Ownership checks

---

### Admin Dashboard

The administrative dashboard provides:

- Total products
- Active products
- Inactive products
- Total orders
- Pending orders
- Confirmed orders
- Cancelled orders
- Low-stock products

---

## API Documentation

Interactive API documentation is available during local and development runs.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

The OpenAPI document includes:

- Endpoint summaries and tags
- Request and response schemas
- JSON media types
- JWT Bearer authentication
- Protected-operation security requirements
- Reusable error responses for:
  - `400 Bad Request`
  - `401 Unauthorized`
  - `403 Forbidden`
  - `404 Not Found`
  - `409 Conflict`
  - `500 Internal Server Error`

Swagger UI and OpenAPI endpoints are disabled by the production profile.

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/register` | Register a new user | Public |
| POST | `/auth/login` | Authenticate and receive access and refresh tokens | Public |
| POST | `/auth/refresh` | Issue new tokens using a refresh token | Public |
| POST | `/auth/logout` | Revoke a refresh token | Public |
| POST | `/auth/logout-all` | Revoke all active sessions | Authenticated |
| GET | `/auth/sessions` | List active authentication sessions | Authenticated |
| DELETE | `/auth/sessions/{sessionId}` | Revoke a specific session | Authenticated |
| GET | `/auth/me` | Get the authenticated user | Authenticated |
| POST | `/auth/verify` | Verify an email address | Public |
| POST | `/auth/forgot` | Request a password reset | Public |
| POST | `/auth/reset` | Reset a password | Public |

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

### Shopping Cart

| Method | Endpoint | Description |
|---|---|---|
| GET | `/cart` | Get the authenticated user's cart |
| POST | `/cart/items` | Add an item to the cart |
| PUT | `/cart/items/{itemId}` | Update a cart item quantity |
| DELETE | `/cart/items/{itemId}` | Remove a cart item |
| DELETE | `/cart` | Clear the cart |
| POST | `/cart/checkout` | Convert the cart into an order |

---

### Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/order` | Create an order |
| GET | `/order` | Get the authenticated user's orders |
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
  "accessToken": "ACCESS_TOKEN",
  "refreshToken": "REFRESH_TOKEN"
}
```

Use the access token on protected endpoints:

```http
Authorization: Bearer ACCESS_TOKEN
```

Refresh the authentication session:

```http
POST /auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

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

## Database Management

The project uses **Flyway** to manage PostgreSQL schema changes.

Hibernate validates the database schema:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway manages:

- Initial database schema
- User roles and integrity constraints
- Case-insensitive username and email uniqueness
- Product price precision
- Product activation and stock fields
- Stock constraints and optimistic locking
- Order status constraints
- Product and shipping snapshots
- Order totals
- Password reset versioning
- Refresh-token session storage
- Persistent shopping carts

Never modify an already-applied migration. Every database change must use a new migration version.

---

## Automated Testing

The test suite covers:

- Authentication and authorization
- Registration validation
- Password policy
- Access and refresh token flows
- JWT token types
- Active session management
- Single-use password reset tokens
- Email verification
- Forgot-password privacy
- User identifier normalization
- Product validation
- Review validation
- Pagination validation
- Shopping cart operations
- Cart checkout
- Order price snapshots
- Order total snapshots
- Shipping address snapshots
- Stock concurrency
- Product status restrictions
- Security responses
- CORS configuration
- Security headers
- OpenAPI documentation
- Production profile configuration
- Service transactions

Run all tests:

```powershell
.\mvnw.cmd clean verify
```

---

## Running Locally

### Prerequisites

- Java 21
- PostgreSQL
- Git

The project includes the Maven Wrapper, so a global Maven installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/mohameedhendy/E-Commerce-System.git
cd E-Commerce-System
```

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE ecommerce_db;
```

### 3. Configure Environment Variables

PowerShell example:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/ecommerce_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-database-password"

$env:JWT_SECRET="replace-with-a-random-secret-that-has-at-least-32-characters"
$env:JWT_ISSUER="eCommerce"
$env:JWT_EXPIRY_SECONDS="3600"

$env:MAIL_HOST="localhost"
$env:MAIL_PORT="1025"
$env:EMAIL_FROM="no-reply@ecommerce.com"

$env:EMAIL_VERIFICATION_ENABLED="false"
$env:FRONTEND_URL="http://localhost:3000"
```

Do not commit real passwords or JWT secrets.

### 4. Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

---

## Docker

Copy the environment template:

```powershell
Copy-Item .env.example .env
```

Update `.env` with local development values, then start the configured Docker Compose services:

```powershell
docker compose up --build -d
```

Check service status:

```powershell
docker compose ps
```

View backend logs:

```powershell
docker compose logs -f backend
```

Stop the stack:

```powershell
docker compose down
```

Do not commit the real `.env` file.

---

## Production Profile

Activate the production profile using:

```text
SPRING_PROFILES_ACTIVE=prod
```

The production configuration includes:

- Externalized secrets and environment variables
- Database schema validation
- Disabled Swagger UI and OpenAPI endpoints
- Restricted error details
- Production security headers
- Actuator health probes
- Docker health checks

Health endpoint:

```text
/actuator/health
```

Probe endpoints:

```text
/actuator/health/liveness
/actuator/health/readiness
```

---

## CI/CD

GitHub Actions workflows provide:

### Backend CI

Runs on pushes and pull requests and performs:

```powershell
.\mvnw.cmd clean verify
```

This validates compilation, automated tests, and production configuration.

### Container Publishing

The container workflow:

- Builds the backend Docker image
- Publishes images to GitHub Container Registry
- Uses repository-scoped GitHub authentication
- Runs according to the workflow's configured push, tag, or manual triggers

---

## Project Structure

```text
src/
|-- main/
|   |-- java/
|   |   `-- com/ecommerce/ecommerce_backend/
|   |       |-- config/
|   |       |-- controller/
|   |       |-- dao/
|   |       |-- dto/
|   |       |-- exception/
|   |       |-- model/
|   |       |-- security/
|   |       `-- service/
|   `-- resources/
|       |-- db/
|       |   `-- migration/
|       |-- application.properties
|       `-- application-prod.properties
`-- test/
    |-- java/
    `-- resources/
```

Additional root-level infrastructure:

```text
.github/workflows/   GitHub Actions workflows
Dockerfile           Backend container image
compose.yaml         Local multi-container stack
.env.example         Environment variable template
mvnw / mvnw.cmd      Maven Wrapper
```

---

## Security Notes

- Never commit real secrets.
- Use a random JWT secret with at least 32 characters.
- Use HTTPS in production.
- Restrict `FRONTEND_URL` to trusted origins.
- Keep production Swagger endpoints disabled.
- Rotate production credentials when necessary.
- Use the production profile outside local development.