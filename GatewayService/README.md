# Gateway Service

## Purpose

The Gateway Service acts as the **single entry point** for all client traffic to the microservices ecosystem. Its responsibilities include:

- **Route requests**: Direct incoming API calls to the appropriate microservice (REST API, Product Service, Auth Service, etc.).
- **Authentication & Authorization**: It is responsible for request routing, session resolution, authentication enforcement, authorization checks, and identity propagation to downstream services.
- **Rate limiting & throttling**: Prevent abuse or overload from high-volume traffic.
- **API versioning and aggregation**: Support multiple API versions and optionally aggregate responses from multiple services.
- **Provide a unified public interface**: Expose a clean, consistent API to web, mobile, and third-party clients.

## Challenges

The Gateway Service faces unique challenges as it is **exposed to the internet**:

- High request volume from multiple clients, including web, mobile, and partners.
- Latency sensitivity: Users expect fast responses, especially for interactive operations.
- Security: Malicious actors can attempt token abuse, DoS attacks, or probing of backend services.
- Reliability: Failure in the gateway can affect all downstream services, making it a critical point of failure.


## Solution for Problems

To ensure high availability, security, and performance, the Gateway Service implements several strategies:

1. **Stateless Design**
    - Does not store session or business data.
    - Can be scaled horizontally without complex state management.

2. **Authentication & Authorization Enforcement**
    - Read cookie
    - Authenticated session validation against Redis
    - Build security context
    - Checks user roles and permissions before routing requests to internal services.

> ** API Endpoints Documentation**: See [ENDPOINTS.md](./ENDPOINTS.md) for the complete API endpoint reference including routes, authorization rules, request/response examples, and rate limiting.

3. **Rate Limiting & Throttling**
    - Protects backend services from overload.
    - **IP-based rate limiting** for authentication endpoints (login, registration) as first line of defense.
    - Per-user and per-endpoint rate limiting for authenticated requests.
    - AuthService handles **user-based rate limiting** that requires user identification (see [AuthService/README.md](../AuthService/README.md)).

4. **Caching & Response Aggregation**
    - Caches frequently requested public data to reduce load on downstream services.
    - Optionally aggregates responses from multiple services to reduce client latency.

5. **Observability & Logging**
    - Send async metrics and logs for request tracing to InfluxDb/Promethius so Grafana san have statitics.
    - Grafana in turn will be able to have alerts on abnormal request patterns, latency spikes, or failures.

6. **Session Types Managed by Gateway**
   - Anonymous Session (Gateway-owned) is created when incoming request has no anonymous cookie (cart ownership `cart_anonymous` tables in Cassandra)
   - for authenticated Session (AuthService-owned) reads cookie, validate session against Redis and build security context
   - Gateway never creates or mutates authenticated sessions.

7. **Internal Service Authentication**
   - All requests to AuthService must include `X-Gateway-Request` header with value from `GATEWAY_SERVICE_SECRET` environment variable
   - AuthService validates both header value and source IP address (whitelist includes `gateway-service`, `127.0.0.1`, `localhost`, `172.18.0.0/16`)
   - Supports both local development and Docker environments
   - Spring Cloud OpenFeign automatically adds the header via `AuthServiceFeignConfig` interceptor
   - IP whitelist configurable via `GATEWAY_ALLOWED_IPS` environment variable
   - Both services must use the same `GATEWAY_SERVICE_SECRET` value


## Infrastructure Recommendations

To maximize **availability, scalability, and security**, the Gateway Service should be deployed with:

- **Dedicated Cloud Ingress**
    - Exposes API endpoints to the internet.
    - Supports HTTPS termination and routing.

- **Global Load Balancer / CDN**
    - Distributes requests across multiple regions or instances.
    - Provides DDoS protection and caching for static endpoints.

- **API Gateway / Reverse Proxy**
    - Enforces rate limiting, request validation, and routing.
    - Supports authentication enforcement before hitting backend services.

- **Horizontal Scaling**
    - Stateless design allows scaling out automatically based on traffic spikes.

- **Separation from Internal Services**
    - Does **not** share runtime or resources with internal services. so it minimizes blast radius in case of failure or attack.



## Protection Layers
1. Gateway Rate Limiting (Redis-based):
   - **Auth Endpoints (IP-based)**:
     - Login: 10 failed attempts per 15 minutes per IP → temporary IP block (1 hour)
     - Registration: 5 registrations per IP per hour
     - Session refresh: 10 requests per session per minute
   - **Cart Operations**:
     - Anonymous users: 10 cart operations per minute per IP
     - Authenticated users: 100 cart operations per minute per user
     - Global anonymous limit cart creates per second
   - **Other Endpoints**: See [ENDPOINTS.md](./ENDPOINTS.md) for complete rate limiting rules

> **Note**: Gateway handles IP-based rate limiting for auth endpoints. AuthService handles user-based rate limiting (per-user login attempts, password reset) that requires user identification. See [AuthService/README.md](../AuthService/README.md) for details.

2. Device Fingerprinting:
   - Generate fingerprint from: IP + User-Agent + Accept-Language + Screen Resolution
   - Store in Redis with rate limit counter
   - Block fingerprints creating >5 carts in 5 minutes
> We will also add request validation and sanitization especially for anonymous users

## Summary

The Gateway Service is the **critical interface between external clients and internal microservices**:

- It provides a **secure, scalable, and observable entry point**.
- Handles authentication, authorization, and traffic shaping.
- Anonymous and authenticated sessions are clearly separated
- Ensures backend services remain protected and resilient as downstream services remain stateless and simple.
- Scales independently to handle varying traffic patterns without impacting business logic services.


```
┌─────────────────┐
│  Gateway Service│
│  (Rate Limiting)│
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌───▼────┐
│ Redis │ │ Redis  │
│ Cache │ │ Rate   │
│       │ │ Limit  │
└───┬───┘ └────────┘
    │
    │
┌───▼──────────────────────────────────────────────┐
│         Service Layer                            │
├──────────────────────────────────────────────────┤
│ AuthService        -> user_db                    │
│ APIService         -> TO-DO: decide all Dbs      │
│ ProductService     -> product_db                 │
│ PaymentService     -> order_db                   │
│ CartService        -> Cassandra Cluster          │
│ AnalyticsService   -> ClickHouse (OLAP)          │
└──────────────────────────────────────────────────┘
```