# AuthService

## Purpose

The **AuthService** is the single source of truth for **user identity, authentication, and authorization metadata** in the system. It is responsible for validating user credentials, issuing and revoking authenticated sessions, and exposing user identity to the **Gateway**, which enforces access control for all downstream services.

AuthService is intentionally **not on the hot path** for every request. After login, request authentication is handled via Redis-backed sessions validated at the Gateway layer.


## Challenges

Designing authentication for this platform introduces several challenges:
* Support **both anonymous and authenticated users** (cart access)
* Work seamlessly with **Next.js ISR / SSR and CDN caching**
* Avoid client-side token exposure (XSS risks)
* Enable **instant logout and session revocation**
* Scale across microservices without coupling
* Keep authentication logic **centralized** and business services **simple and small**


## Solution for Problems

### High-Level Architecture

```
Client
  │
  ▼
Gateway Service (Port 8080)
  │
  │ (forwards /auth/* requests)
  ▼
AuthService (Port 8081)
  │
  ├──► PostgreSQL (auth_db :5432)
  │     ├── users, roles, permissions
  │     └── refresh_tokens, authentication_events (audit)
  │
  └──► Redis (redis-session :6381)
        └── session metadata (TTL-based)
```

**Flow:**
1. Client submits credentials via Gateway
2. Gateway forwards to AuthService
3. AuthService validates credentials against PostgreSQL
4. AuthService creates session in Redis
5. AuthService returns `auth_session_id` cookie
6. Gateway validates sessions from Redis on subsequent requests


The solution is based on:
* **Session-based authentication using HttpOnly cookies** for browsers
* **Redis-backed session storage** for fast validation and revocation
* **Gateway-enforced authorization**
* **JWT only for internal/service contexts (optional)**

AuthService issues identity; the Gateway enforces it.

## Authentication
Authentication is split across two components:
* **AuthService**: validates credentials and issues authenticated sessions
* **Gateway**: manages anonymous sessions and enforces authentication on requests

The AuthService never sees anonymous traffic and is not responsible for routing or request forwarding.


### AuthService Cookie Generation
AuthService is responsible **only** for generating and managing the authenticated session cookie.

#### Authenticated Session Cookie

```
auth_session_id = <secure-random-token>
HttpOnly; Secure; SameSite=Lax; Path=/
```

Characteristics:
* Generated only after successful login
* Contains no user data
* XSS protection with **HttpOnly** cookies cannot be accessed by JS
* References a Redis-backed session meaning Session invalidation is possible via Redis entry deletion
* Simplicity with no client token handling 

Cookie lifetime matches Redis TTL.


## Authorization

### Model
* Role-Based Access Control (RBAC)
* Roles and permissions stored in PostgreSQL
* Resolved once at login time

Authorization is enforced **only at the Gateway**.

Gateway logic:
* Validate session
* Build security context
* Enforce route-level access rules
* Forward identity headers to services

Downstream services **trust the Gateway** and do not re-check auth.

## Monitoring & Observability

Monitoring is critical to ensure reliability, security, and performance of authentication operations.

### Key Metrics to Track

1. **Authentication Performance**
   * Login success/failure rates
   * Session creation rate
   * Session revocation rate

2. **Security Metrics**
   * Failed login attempts per user/IP
   * Account lockout events withs uspicious authentication patterns
   * Session hijacking attempts

3. **Operational Metrics**
   * Active session count
   * Session expiration rate
   * Cookie generation failures
   * Rate limiting triggers

## Monitoring

Critical alerts should be configured for:
* **High authentication failure rates** - Potential brute force attacks
* **Redis connectivity issues** - Session storage unavailable
* **Database connection problems** - Cannot validate credentials
* **Unusual authentication patterns** - Potential security incidents
* **Session storage capacity** - Redis memory approaching limits

> Prometheus/InfluxDB can be used for metrics collection and Grafana dashboards for visualization and alerting


## API Endpoints

The AuthService exposes the following endpoints for authentication operations:

### Authentication Endpoints

* **`POST /auth/login`**
  * Validates user credentials (email/username + password)
  * Creates authenticated session in Redis
  * Returns `auth_session_id` HttpOnly cookie
  * Returns user identity and roles

* **`POST /auth/logout`**
  * Revokes current session
  * Deletes session from Redis
  * Clears `auth_session_id` cookie

* **`POST /auth/refresh`** (Optional)
  * Extends session lifetime
  * Updates Redis TTL
  * Returns new cookie if applicable

### Session Management Endpoints

* **`GET /auth/session/validate`** (Internal)
  * Validates session token
  * Returns user identity and roles
  * Used by Gateway for request authorization
  * Not exposed to external clients

* **`GET /auth/session/current`**
  * Returns current authenticated user information
  * Requires valid session cookie

## Database Schema Reference

The AuthService uses PostgreSQL (`auth_db`) for persistent storage and Redis for session management.

### PostgreSQL Tables

* **`users`**: User accounts with credentials, profile information, and account status
* **`roles`**: System roles (ROLE_ADMIN, ROLE_USER, ROLE_MERCHANT, ROLE_SUPPORT)
* **`user_roles`**: Many-to-many relationship between users and roles
* **`permissions`**: Fine-grained permissions (resource + action)
* **`role_permissions`**: Many-to-many relationship between roles and permissions
* **`authentication_events`**: Audit log for authentication events (login, logout, account lockouts)
* **`ip_login_attempts`**: IP-based login attempt tracking for security monitoring

### Redis Storage

* **Session Metadata**: Stored in `redis-session` instance
  * Key format: `session:{auth_session_id}`
  * Value: JSON containing user ID, roles, permissions, expiration timestamp
  * TTL: Matches cookie lifetime (e.g., 24 hours)

### Key Design Decisions

* **Sessions in Redis, not PostgreSQL**: Enables fast validation and instant revocation
* **PostgreSQL for audit**: Authentication events are logged to `authentication_events` table for audit trail
* **Role resolution at login**: Roles and permissions are resolved once and stored in Redis session

## Summary

The AuthService is the **single source of truth for user identity, authentication, and authorization metadata** in the AbstractTrade platform:

* It provides **secure, session-based authentication** using HttpOnly cookies and Redis-backed session storage
* It is **intentionally not on the hot path** for every request—after login, session validation happens at the Gateway layer
* It maintains a **clear separation of concerns**: AuthService issues identity, Gateway enforces it
* It supports **both anonymous and authenticated users** while keeping authentication logic centralized
* It enables **instant logout and session revocation** through Redis-based session management
* It scales independently with a **stateless design** that allows horizontal scaling without session affinity

By combining PostgreSQL for persistent identity data, Redis for fast session storage, and a Gateway-enforced authorization model, the system achieves:
* **Security**: HttpOnly cookies prevent XSS attacks, centralized revocation prevents token abuse
* **Performance**: Session validation happens at the Gateway using Redis, avoiding database queries on every request
* **Scalability**: Stateless design allows horizontal scaling, Redis enables fast session operations
* **Simplicity**: Business services trust the Gateway and do not need to implement authentication logic

This design ensures the authentication platform remains **secure, performant, and maintainable** while supporting the evolving needs of the e-commerce platform.
