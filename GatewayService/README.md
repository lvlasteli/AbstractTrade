# Gateway Service

## Purpose

The Gateway Service acts as the **single entry point** for all client traffic to the microservices ecosystem. Its responsibilities include:

- **Route requests**: Direct incoming API calls to the appropriate microservice (REST API, Product Service, Auth Service, etc.).
- **Authentication & Authorization**: Validate JWT tokens, OAuth2 tokens, and enforce role-based access control for protected endpoints.
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
    - Validates JWT/OAuth2 tokens. (TO-DO decide)
    - Checks user roles and permissions before routing requests to internal services.

3. **Rate Limiting & Throttling**
    - Protects backend services from overload.
    - Limits per-user, per-IP, or per-endpoint request rates.

4. **Caching & Response Aggregation**
    - Caches frequently requested public data to reduce load on downstream services.
    - Optionally aggregates responses from multiple services to reduce client latency.

5. **Observability & Logging**
    - Send async metrics and logs for request tracing to InfluxDb/Promethius so Grafana san have statitics.
    - Grafana in turn will be able to have alerts on abnormal request patterns, latency spikes, or failures.


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
   - Anonymous users: 10 cart operations per minute per IP
   - Authenticated users: 100 cart operations per minute per user
   - Global anonymous limit cart creates per second

2. Device Fingerprinting:
   - Generate fingerprint from: IP + User-Agent + Accept-Language + Screen Resolution
   - Store in Redis with rate limit counter
   - Block fingerprints creating >5 carts in 5 minutes
> We will also add request validation and sanitization especially for anonymous users

## Summary

The Gateway Service is the **critical interface between external clients and internal microservices**:

- It provides a **secure, scalable, and observable entry point**.
- Handles authentication, authorization, and traffic shaping.
- Ensures backend services remain protected and resilient.
- Scales independently to handle varying traffic patterns without impacting business logic services.