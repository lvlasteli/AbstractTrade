# Listener Service

## Purpose

The Listener Service is responsible for handling external events from third-party systems in a robust, secure, and scalable manner. Its main responsibilities include:

- **Receive webhooks**: Accept incoming HTTP requests from external providers or partners.
- **Validate signatures / secrets**: Ensure that incoming requests are authentic and originate from trusted sources.
- **Handle retries and duplicates**: Safely process repeated or duplicate webhook calls without causing data inconsistencies.
- **Normalize payloads**: Transform different third-party payload formats into a consistent internal structure suitable for further processing.
- **Produce Kafka events**: Publish validated and normalized events to Kafka topics, making them available for downstream services (e.g., Payment, Product, Notification).

## Challenges

Webhooks are notoriously unreliable. Third-party systems often:

- Retry aggressively, sometimes violating expected backoff rules.
- Ignore backoff or retry guidance, sending requests in bursts.
- Send duplicate events, either intentionally or due to network issues.
- Send malformed or incomplete payloads.

These behaviors can cause issues like duplicate processing, corrupted events, and unexpected load spikes if not properly managed.


## Solution for Problems

To address the inherent challenges of webhooks, the Listener Service is designed with several key solutions:

1. **Idempotency**  
   Every incoming webhook is processed in a way that ensures repeated calls with the same data will not cause duplicate side effects.

2. **Asynchronous Processing**  
   Webhook payloads are processed asynchronously:
    - Requests are quickly acknowledged.
    - Events are pushed to Kafka for downstream services, preventing long blocking operations and improving reliability.

3. **Minimal Business Logic**  
   The Listener only validates, normalizes, and publishes events. All complex business logic is delegated to downstream services. This reduces the blast radius of failures and simplifies scaling.

### General Approach for Idempotency
1. Most webhook providers include some identifier:
   - event_id
   - delivery_id
   - request_id
> If 3rd party doesnt provide something unique we may hash the payload and compare 2 hashes to conclude if they are identical.

2. Persist Received Event IDs in Redis (With TTL)
- Extract event_id (or compute hash)
- Check Redis: If already processed → ignore / ack (return 200 to webhook) or not add entry
- Publish to Kafka
- Mark event as processed (write event_id to store)
> TTL for processed IDs: Webhooks usually retry within minutes/hours. You can expire old event_id records to save storage.
3. Use Kafka Producer Idempotency
- Kafka itself supports idempotent producers with Enable `enable.idempotence = true` in the Kafka producer config
> This adds another safety layer: even if your Listener accidentally retries a publish, Kafka won’t store duplicates.
4. Handle Async Processing because Listener is async:
- Do not block webhook request waiting for downstream services, only persist `event_id` after successful Kafka write.
- Return 200 OK quickly to avoid triggering provider retries.

## Infrastructure Recommendations

To maximize security, reliability, and maintainability, the Listener Service should be deployed with the following infrastructure setup:

- **IP Allowlist**: Only allow traffic from known third-party sources to reach the Listener service.
- **Dedicated Ingress**: Use a dedicated cloud ingress/load balancer to isolate webhook traffic from user-facing traffic.
- **Separate Domain**: Host the Listener service on a dedicated subdomain (e.g., `webhooks.yourdomain.com`) to clearly separate concerns and simplify routing.
- **Cloud Security Best Practices**: Combine HTTPS enforcement, secret validation, and rate limiting to prevent abuse.


## Summary

The Listener Service acts as a reliable bridge between external webhook sources and the internal event-driven architecture:

- It **protects internal systems** from untrusted or poorly behaved third-party requests.
- It **ensures consistent and safe event delivery** to Kafka.
- It **isolates external webhook traffic** from core user-facing services to maintain high availability and operational clarity.

By following the above infrastructure guidelines and architectural patterns, the Listener Service becomes a **scalable, secure, and resilient component** of the microservices ecosystem.
