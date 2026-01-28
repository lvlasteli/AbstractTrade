# Gateway Service - API Endpoints Documentation

This document defines all endpoints exposed by the Gateway Service and their routing to downstream microservices.

## Table of Contents

- [Authorization](#authorization)
- [Authentication Routes](#1-authentication-routes)
- [Public Routes](#2-public-routes)
- [Cart Routes](#3-cart-routes)
- [Order Routes](#4-order-routes)
- [Product Routes](#5-product-routes)
- [Payment Routes](#6-payment-routes)
- [User Profile Routes](#7-user-profile-routes)
- [Analytics Routes](#8-analytics-routes)
- [Logistics Routes](#9-logistics-routes)
- [Notification Routes](#10-notification-routes)
- [Gateway Internal Endpoints](#11-gateway-internal-endpoints)
- [Identity Headers](#identity-headers-forwarded-to-services)

---

## Authorization

```
┌───────────────────────────────────────────────────────────────┐
│                   GATEWAY AUTHORIZATION RULES                 │
├───────────────────────────────────────────────────────────────┤
│  /public/**       →  public()                                 │
│  /auth/login      →  public()                                 │
│  /auth/register   →  public()                                 │
│  /cart/**         →  public&authenticated()                   │
│  /orders/**       →  authenticated()                          │
│  /users/**        →  authenticated()                          │
│  /payments/**     →  authenticated()                          │
│  /products (GET)  →  permitAll()                              │
│  /products (W)    →  hasRole('MERCHANT')                      │
│  /analytics/**    →  hasRole('ADMIN')                         │
│  /logistics/**    →  authenticated() + role checks            │
│  /notifications/**→ authenticated()                           │
└───────────────────────────────────────────────────────────────┘
```

---

## 1. Authentication Routes

**Target Service**: AuthService (Port 8081)

Gateway forwards `/auth/*` requests to AuthService for credential validation and session management.

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/auth/register` | POST | No | Register new user account |
| `/auth/login` | POST | No | Validate credentials, create session in Redis, return `auth_session_id` cookie |
| `/auth/logout` | POST | Yes | Revoke session, delete from Redis, clear cookie |
| `/auth/refresh` | POST | Yes | Extend session lifetime, update Redis TTL |
| `/auth/session/current` | GET | Yes | Get current authenticated user info and roles |
| `/auth/password/forgot` | POST | No | Initiate password reset flow |
| `/auth/password/reset` | POST | No | Complete password reset with token |

### Internal Endpoints (Not exposed to clients)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/session/validate` | GET | Gateway validates session token against Redis |

### Request/Response Examples

**POST /auth/login**
```json
// Request
{
  "email": "user@example.com",
  "password": "securePassword123"
}

// Response (200 OK)
// Set-Cookie: auth_session_id=<secure-random-token>; HttpOnly; Secure; SameSite=Lax; Path=/
{
  "userId": "uuid-here",
  "email": "user@example.com",
  "roles": ["ROLE_USER"],
  "permissions": ["cart:read", "cart:write", "orders:read", "orders:write"]
}
```

**POST /auth/logout**
```json
// Response (200 OK)
// Set-Cookie: auth_session_id=; Max-Age=0; HttpOnly; Secure; SameSite=Lax; Path=/
{
  "message": "Logged out successfully"
}
```

---

## 2. Public Routes

**Target Service**: Various (No authentication required)

| Endpoint | Method | Service | Description |
|----------|--------|---------|-------------|
| `/public/products` | GET | Product Service :8083 | List products (paginated) |
| `/public/products/{id}` | GET | Product Service :8083 | Get product details |
| `/public/products/categories` | GET | Product Service :8083 | List product categories |
| `/public/products/search` | GET | Product Service :8083 | Search products |
| `/public/health` | GET | Gateway | Gateway health check |

### Caching Strategy
- Product catalog responses cached in `redis-cache` for 5-15 minutes
- Category list cached for 60 minutes
- Search results cached for 5 minutes

---

## 3. Cart Routes

**Target Service**: Cart Service (Port 8084)

Supports both **anonymous** and **authenticated** users. Gateway manages anonymous sessions.

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/cart` | GET | Anon/Auth | Get current cart |
| `/cart/items` | POST | Anon/Auth | Add item to cart |
| `/cart/items/{itemId}` | PUT | Anon/Auth | Update item quantity |
| `/cart/items/{itemId}` | DELETE | Anon/Auth | Remove item from cart |
| `/cart/clear` | DELETE | Anon/Auth | Clear all items from cart |
| `/cart/merge` | POST | Auth | Merge anonymous cart with user cart (called after login) |
| `/cart/saved-for-later` | GET | Auth | Get saved for later items |
| `/cart/saved-for-later/{itemId}` | POST | Auth | Move item to saved for later |
| `/cart/saved-for-later/{itemId}/restore` | POST | Auth | Move saved item back to cart |

### Gateway Responsibilities

1. **Anonymous Session Management**
   - Create `anon_session_id` cookie if not present
   - Store anonymous session in Redis with device fingerprint

2. **Rate Limiting (Redis-based)**
   - Anonymous users: 10 cart operations per minute per IP
   - Authenticated users: 100 cart operations per minute per user
   - Global limit: Cart creates per second (configurable)

3. **Device Fingerprinting**
   - Generate fingerprint from: `IP + User-Agent + Accept-Language`
   - Block fingerprints creating >5 carts in 5 minutes

### Request/Response Examples

**POST /cart/items**
```json
// Request
{
  "productId": "prod-uuid",
  "quantity": 2,
  "variantId": "variant-uuid" // optional
}

// Response (201 Created)
{
  "cartId": "cart-uuid",
  "items": [
    {
      "itemId": "item-uuid",
      "productId": "prod-uuid",
      "productName": "Product Name",
      "quantity": 2,
      "unitPrice": 29.99,
      "totalPrice": 59.98
    }
  ],
  "itemCount": 2,
  "subtotal": 59.98
}
```

---

## 4. Order Routes

**Target Service**: API Service (Port 8082)

**Authentication**: Required for all endpoints

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/orders` | GET | USER | Get user's orders (paginated) |
| `/orders` | POST | USER | Create new order from cart |
| `/orders/{orderId}` | GET | USER | Get order details |
| `/orders/{orderId}/cancel` | POST | USER | Cancel order (if cancellable) |
| `/orders/{orderId}/items` | GET | USER | Get order items |
| `/orders/{orderId}/tracking` | GET | USER | Get order tracking info |

### Admin/Support Endpoints

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/admin/orders` | GET | ADMIN/SUPPORT | List all orders (filtered) |
| `/admin/orders/{orderId}` | GET | ADMIN/SUPPORT | Get any order details |
| `/admin/orders/{orderId}/status` | PUT | ADMIN/SUPPORT | Update order status |

### Request/Response Examples

**POST /orders**
```json
// Request
{
  "shippingAddressId": "address-uuid",
  "billingAddressId": "address-uuid",
  "paymentMethodId": "payment-uuid",
  "notes": "Please leave at door"
}

// Response (201 Created)
{
  "orderId": "order-uuid",
  "orderNumber": "ORD-2026-00001234",
  "status": "PENDING",
  "items": [...],
  "subtotal": 59.98,
  "shipping": 5.99,
  "tax": 7.20,
  "total": 73.17,
  "createdAt": "2026-01-21T10:30:00Z"
}
```

---

## 5. Product Routes

**Target Service**: Product Service (Port 8083)

| Endpoint | Method | Auth | Role | Description |
|----------|--------|------|------|-------------|
| `/products` | GET | No | - | List products (paginated) |
| `/products/{id}` | GET | No | - | Get product details |
| `/products/search` | GET | No | - | Search products |
| `/products/categories` | GET | No | - | List categories |
| `/products/categories/{slug}` | GET | No | - | Get category with products |
| `/products` | POST | Yes | MERCHANT | Create product |
| `/products/{id}` | PUT | Yes | MERCHANT | Update product |
| `/products/{id}` | DELETE | Yes | MERCHANT | Delete product |
| `/products/{id}/stock` | GET | Yes | MERCHANT | Get stock info |
| `/products/{id}/stock` | PUT | Yes | MERCHANT | Update stock |
| `/products/{id}/images` | POST | Yes | MERCHANT | Upload product images |

## 6. Payment Routes

**Target Service**: Payment Service (Port 8085)

**Authentication**: Required for all endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/payments/methods` | GET | Get user's saved payment methods |
| `/payments/methods` | POST | Add new payment method |
| `/payments/methods/{id}` | DELETE | Remove payment method |
| `/payments/methods/{id}/default` | PUT | Set as default payment method |
| `/payments/process` | POST | Process payment for order |
| `/payments/{orderId}/status` | GET | Get payment status for order |
| `/payments/{orderId}/refund` | POST | Request refund (if eligible) |

### Request/Response Examples

**POST /payments/process**
```json
// Request
{
  "orderId": "order-uuid",
  "paymentMethodId": "payment-uuid",
  "amount": 73.17,
  "currency": "EUR"
}

// Response (200 OK)
{
  "transactionId": "txn-uuid",
  "orderId": "order-uuid",
  "status": "COMPLETED",
  "amount": 73.17,
  "currency": "EUR",
  "processedAt": "2026-01-21T10:35:00Z"
}
```

---

## 7. User Profile Routes

**Target Service**: API Service (Port 8082)

**Authentication**: Required for all endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/users/me` | GET | Get current user profile |
| `/users/me` | PUT | Update profile |
| `/users/me/password` | PUT | Change password |
| `/users/me/addresses` | GET | Get user addresses |
| `/users/me/addresses` | POST | Add new address |
| `/users/me/addresses/{id}` | PUT | Update address |
| `/users/me/addresses/{id}` | DELETE | Delete address |
| `/users/me/addresses/{id}/default` | PUT | Set default address |
| `/users/me/preferences` | GET | Get user preferences |
| `/users/me/preferences` | PUT | Update preferences |

---

## 8. Analytics Routes

**Target Service**: Analytics Service (Port 8088)

**Authentication**: Required  
**Role**: ADMIN only

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/analytics/dashboard` | GET | Dashboard overview metrics |
| `/analytics/orders` | GET | Order analytics (revenue, counts) |
| `/analytics/orders/trends` | GET | Order trends over time |
| `/analytics/products` | GET | Product performance analytics |
| `/analytics/products/top` | GET | Top selling products |
| `/analytics/users` | GET | User analytics (registrations, activity) |
| `/analytics/users/cohorts` | GET | User cohort analysis |
| `/analytics/revenue` | GET | Revenue analytics |
| `/analytics/revenue/by-category` | GET | Revenue breakdown by category |

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `startDate` | date | Start of date range (ISO 8601) |
| `endDate` | date | End of date range (ISO 8601) |
| `granularity` | string | `hour`, `day`, `week`, `month` |

---

## 9. Logistics Routes

**Target Service**: Logistics Service (Port 8087)

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/logistics/shipments/{orderId}` | GET | USER | Get tracking info for user's order |
| `/logistics/shipments/{orderId}/label` | GET | USER | Download shipping label (if available) |

### Admin/Support Endpoints

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/admin/logistics/shipments` | GET | ADMIN/SUPPORT | List all shipments |
| `/admin/logistics/shipments/{id}` | GET | ADMIN/SUPPORT | Get shipment details |
| `/admin/logistics/shipments/{id}/status` | PUT | ADMIN/SUPPORT | Update shipment status |
| `/admin/logistics/carriers` | GET | ADMIN | List shipping carriers |

---

## 10. Notification Routes

**Target Service**: Notification Service (Port 8086)

**Authentication**: Required

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/notifications/preferences` | GET | Get notification preferences |
| `/notifications/preferences` | PUT | Update notification preferences |
| `/notifications/history` | GET | Get notification history |
| `/notifications/{id}/read` | PUT | Mark notification as read |

### Admin Endpoints

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/admin/notifications/send` | POST | ADMIN | Send notification to user(s) |
| `/admin/notifications/templates` | GET | ADMIN | List notification templates |
| `/admin/notifications/templates` | POST | ADMIN | Create template |

---

## 11. Gateway Internal Endpoints

These endpoints are for internal use, monitoring, and health checks.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/actuator/health` | GET | Spring Boot health check |
| `/actuator/health/liveness` | GET | Kubernetes liveness probe |
| `/actuator/health/readiness` | GET | Kubernetes readiness probe |
| `/actuator/metrics` | GET | Prometheus metrics endpoint |
| `/actuator/info` | GET | Application info |

---

## Identity Headers Forwarded to Services

When the Gateway routes requests to downstream services, it adds the following headers:

| Header | Description | Example |
|--------|-------------|---------|
| `X-User-Id` | Authenticated user's UUID | `123e4567-e89b-12d3-a456-426614174000` |
| `X-User-Roles` | Comma-separated roles | `ROLE_USER,ROLE_MERCHANT` |
| `X-User-Permissions` | Comma-separated permissions | `cart:read,cart:write,orders:read` |
| `X-Session-Id` | Session identifier | `sess_abc123...` |
| `X-Anon-Session-Id` | Anonymous session ID (if applicable) | `anon_xyz789...` |
| `X-Request-Id` | Unique request ID for tracing | `req_def456...` |
| `X-Forwarded-For` | Original client IP | `192.168.1.1` |

Downstream services **trust these headers** and do not re-validate authentication.

---

## Error Responses

All endpoints return consistent error responses:

```json
{
  "timestamp": "2026-01-21T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/cart/items",
  "errors": [
    {
      "field": "quantity",
      "message": "must be greater than 0"
    }
  ]
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (no valid session) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 409 | Conflict (e.g., duplicate resource) |
| 429 | Too Many Requests (rate limited) |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

---

## Rate Limiting

Gateway implements rate limiting using Redis (`redis-ratelimit`):

| Endpoint Pattern | Anonymous | Authenticated | Window |
|------------------|-----------|---------------|--------|
| `/cart/**` | 10 req/min/IP | 100 req/min/user | 1 minute |
| `/auth/login` | 5 req/min/IP | - | 1 minute |
| `/orders/**` | - | 30 req/min/user | 1 minute |
| `/products` (GET) | 60 req/min/IP | 120 req/min/user | 1 minute |
| `/payments/**` | - | 10 req/min/user | 1 minute |
| Global | 1000 req/min/IP | 5000 req/min/user | 1 minute |

Rate limit headers returned:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1706004660
```

---

## Service Port Reference

| Service | Port | Database |
|---------|------|----------|
| Gateway | 8080 | - |
| Auth | 8081 | PostgreSQL (auth_db :5432), Redis (session :6381) |
| API | 8082 | PostgreSQL (auth_db, order_db) |
| Product | 8083 | PostgreSQL (product_db :5433), Redis (cache :6379) |
| Cart | 8084 | Cassandra (cart_keyspace), Redis (ratelimit :6380) |
| Payment | 8085 | PostgreSQL (payment_db :5435) |
| Notification | 8086 | PostgreSQL (notification_db :5436) |
| Logistics | 8087 | PostgreSQL (product_db :5433) |
| Analytics | 8088 | ClickHouse, PostgreSQL (order_db :5434) |
| Listener | 8089 | Kafka |

