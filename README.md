# E-Commerce Backend System

A production-oriented e-commerce REST API built with **Java 21**, **Spring Boot**, **Spring Security**, **JWT**, **PostgreSQL**, and **Flyway**.

This project was developed with support from **AI tools** during code review, testing, and documentation.

The system provides secure authentication, refresh-token session management, persistent shopping carts, product and inventory management, order processing, reviews, administration, database migrations, API documentation, containerization, and automated testing.

---

## Features

### Authentication and Security

- User registration and login
- JWT access-token authentication
- Refresh-token authentication flow
- Persistent authentication sessions
- Refresh-token rotation
- Logout from the current session
- Logout from all sessions
- List active sessions
- Revoke a specific session
- Email verification
- Password reset
- Single-use password-reset tokens
- Separate JWT token types for:
  - Access tokens
  - Email verification
  - Password reset
- Role-based authorization using `USER` and `ADMIN`
- BCrypt password hashing
- Configurable BCrypt strength
- Case-insensitive username and email uniqueness
- Username and email normalization
- Generic forgot-password responses to prevent email enumeration
- Trusted frontend CORS configuration
- Security headers and Content Security Policy
- Unified JSON responses for authentication and authorization errors

---

### Product Management

Public users can:

- List active products
- Search products by keyword
- Sort products by ID, name, or price
- Use pagination
- View product details
- View available stock
- View product reviews

Administrators can:

- Create products
- Update product details
- Soft-delete products
- Restore inactive products
- List active and inactive products
- Filter products by active status
- Update stock through a dedicated endpoint
- View low-stock products

Inactive products cannot be ordered.

---

### Inventory Management

Inventory protection includes:

- Atomic stock updates
- Automatic stock reduction when an order is created
- Automatic stock restoration when an order is cancelled
- Protection against overselling
- Optimistic locking
- Database constraints preventing negative stock
- Concurrent order and cancellation tests
- Separation between product details and stock updates

---

### Shopping Cart

Authenticated users can:

- View their cart
- Add products to the cart
- Update item quantities
- Remove individual items
- Clear the cart
- Checkout the cart into an order

Cart checkout includes:

- Product activity validation
- Available-stock validation
- Current-price validation
- Automatic cart clearing after successful checkout

---

### Address Management

Authenticated users can:

- Add addresses
- Update addresses
- View their addresses

Ownership checks prevent users from accessing or modifying addresses that belong to other users.

Address data is validated before it reaches the database.

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

Review validation includes:

- Rating between 1 and 5
- Required comment
- Comment length limits
- Ownership checks
- Duplicate-review protection
- Purchase eligibility checks

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

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Bean Validation
- Lombok

### Security

- JWT authentication
- BCrypt password hashing
- Role-based authorization
- Refresh-token sessions
- Rate limiting
- Security headers
- Content Security Policy

### Database

- PostgreSQL
- Flyway
- H2 for selected automated tests

### Documentation and Monitoring

- Springdoc OpenAPI
- Swagger UI
- Spring Boot Actuator

### Testing

- JUnit 5
- Mockito
- MockMvc
- GreenMail
- Testcontainers

### DevOps and Automation

- Maven Wrapper
- Docker
- Docker Compose
- GitHub Actions
- GitHub Container Registry
- Dependabot
- Trivy
- JaCoCo
- SpotBugs
- Maven Enforcer

---

## Architecture

The project follows a layered backend architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Main packages:

```text
config       Application configuration
controller   REST API endpoints
dao          Spring Data repositories
dto          Request and response objects
exception    Custom exceptions and global error handling
model        JPA entities
security     Authentication and authorization
service      Business logic and transactions
```

DTOs are used to prevent exposing persistence entities directly through the API.

---

## API Documentation

Interactive API documentation is available during local and development runs.

### Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON

```text
http://localhost:8080/v3/api-docs
```

The OpenAPI document includes:

- Endpoint summaries
- Endpoint tags
- Request schemas
- Response schemas
- JWT Bearer authentication
- Protected-operation security requirements
- Reusable error responses for:
  - `400 Bad Request`
  - `401 Unauthorized`
  - `403 Forbidden`
  - `404 Not Found`
  - `409 Conflict`
  - `500 Internal Server Error`

Swagger UI and OpenAPI endpoints are disabled in the production profile.

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

### Products

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/product` | Search, sort, and paginate active products | Public |
| GET | `/product/{productId}` | Get an active product | Public |
| GET | `/product/{productId}/reviews` | Get product reviews | Public |

Example:

```http
GET /product?keyword=phone&page=0&size=10&sortBy=price&sortDir=desc
```

---

### Reviews

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/product/{productId}/review` | Create a product review | Authenticated |
| PUT | `/reviews/{reviewId}` | Update the authenticated user's review | Authenticated |

---

### Addresses

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/user/{userId}/address` | Get user addresses | Authenticated |
| POST | `/user/{userId}/address` | Add an address | Authenticated |
| PUT | `/user/{userId}/address/{addressId}` | Update an address | Authenticated |

---

### Shopping Cart

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/cart` | Get the authenticated user's cart | Authenticated |
| POST | `/cart/items` | Add an item to the cart | Authenticated |
| PUT | `/cart/items/{itemId}` | Update a cart item quantity | Authenticated |
| DELETE | `/cart/items/{itemId}` | Remove a cart item | Authenticated |
| DELETE | `/cart` | Clear the cart | Authenticated |
| POST | `/cart/checkout` | Convert the cart into an order | Authenticated |

---

### Orders

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/order` | Create an order | Authenticated |
| GET | `/order` | Get the authenticated user's orders | Authenticated |
| GET | `/order/{orderId}` | Get an order | Authenticated |
| PATCH | `/order/{orderId}/cancel` | Cancel an eligible order | Authenticated |

Example request:

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
| POST | `/admin/product` | Create a product with initial stock |
| GET | `/admin/product` | List active and inactive products |
| GET | `/admin/product/{productId}` | Get a product |
| PUT | `/admin/product/{productId}` | Update product details |
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

### Login Request

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

### Login Response

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

### Refresh Request

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
  "path": "/auth/register",
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
- Invalid product status
- Invalid order status transitions
- Optimistic locking conflicts
- Rate-limit violations
- Unexpected application errors

---

## Database Management

The project uses **Flyway** to manage PostgreSQL schema changes.

Hibernate validates the database schema:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway manages:

- Initial database schema
- User roles
- Integrity constraints
- Case-insensitive username and email uniqueness
- Product activation
- Product price precision
- Stock fields and constraints
- Optimistic locking
- Order status constraints
- Product snapshots
- Shipping-address snapshots
- Order totals
- Password-reset versioning
- Refresh-token session storage
- Persistent shopping carts

Every database change must be added through a new migration.

Already-applied migrations must not be modified because Flyway validates migration checksums.

---

## Automated Testing

The project includes unit, integration, security, database, migration, and concurrency tests.

Covered areas include:

- Registration and login
- Authentication and authorization
- Password validation
- Email verification
- Password reset
- Access-token behavior
- Refresh-token behavior
- Active-session management
- User normalization
- Product validation
- Product status restrictions
- Shopping-cart operations
- Cart checkout
- Order creation
- Order cancellation
- Product price snapshots
- Shipping-address snapshots
- Order-total snapshots
- Stock concurrency
- Review validation
- Security error responses
- CORS configuration
- Security headers
- OpenAPI documentation
- Production profile configuration
- PostgreSQL migrations
- Service transactions
- Health endpoints

Run the complete verification process:

```powershell
.\mvnw.cmd verify
```

A global Maven installation is not required because the repository includes the Maven Wrapper.

---

## Running Locally

### Prerequisites

- Java 21
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/mohameedhendy/E-Commerce-System.git
cd E-Commerce-System
```

### 2. Create the Database

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

$env:EMAIL_VERIFICATION_ENABLED="true"
$env:FRONTEND_URL="http://localhost:3000"
```

Email verification is enabled by default.

Set `EMAIL_VERIFICATION_ENABLED=false` only as an explicit local or automated-test override.

Never commit real database passwords, JWT secrets, or production credentials.

### 4. Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

---

## Running with Docker

Copy the environment template:

```powershell
Copy-Item .env.example .env
```

Update `.env` with local values.

Build and start the services:

```powershell
docker compose up --build -d
```

Check the service status:

```powershell
docker compose ps
```

View backend logs:

```powershell
docker compose logs -f backend
```

Stop the services:

```powershell
docker compose down
```

The Docker Compose environment includes:

- PostgreSQL
- Backend application
- Mailpit for local email testing

Do not commit the real `.env` file.

---

## Production Profile

Activate the production profile using:

```text
SPRING_PROFILES_ACTIVE=prod
```

The production profile includes:

- Externalized secrets
- Database schema validation
- Disabled Swagger UI
- Disabled OpenAPI endpoints
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

## CI and Automation

### Backend CI

The backend workflow runs on pushes and pull requests.

It executes:

```powershell
.\mvnw.cmd verify
```

The workflow validates:

- Compilation
- Automated tests
- Code-quality rules
- Database migrations
- Production configuration

### Container Publishing

The container workflow:

- Builds the backend Docker image
- Publishes the image to GitHub Container Registry
- Uses repository-scoped GitHub authentication
- Runs according to the configured push, tag, or manual triggers

### Security and Quality Checks

The repository includes:

- Dependency updates through Dependabot
- Container scanning with Trivy
- Code coverage reporting with JaCoCo
- Static analysis with SpotBugs
- Java and Maven version enforcement

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

Root-level infrastructure:

```text
.github/workflows/   GitHub Actions workflows
Dockerfile           Backend container image
compose.yaml         Local multi-container environment
.env.example         Environment variable template
mvnw / mvnw.cmd      Maven Wrapper
pom.xml              Maven project configuration
```

---

## Security Notes

- Secrets and credentials are managed through environment variables and must not be committed to the repository.
- Production environments should use HTTPS and trusted frontend origins.
- Swagger UI and OpenAPI endpoints are disabled in the production profile.
- Production credentials should be rotated and stored using a secure secrets-management solution.

---

## Repository

GitHub:

```text
https://github.com/mohameedhendy/E-Commerce-System
```