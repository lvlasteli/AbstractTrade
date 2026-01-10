# High-Level Architecture

## 1. Microservices & Kafka Messaging

Kafka is used as the event/message bus in this architecture, which decouples services and allows them to scale independently.

### Services:

- **Auth Service**: Handles authentication (JWT tokens, OAuth2) and authorization (role-based access control).
- **Listener Service**: Handles third-party webhooks, parses them, and sends events/messages to Kafka topics.
- **REST API Service**: Exposes both public and protected endpoints, possibly handling user-related data and authentication.
- **Product Service**: Manages stock and reserves quantities, triggers events like `OrderProcessed` when stock and price calculations are complete.
- **Payment Service**: Listens to the `OrderProcessed` event, verifies the user’s account, and processes payments. Integrates with the Porezna SOAP api.
- **Notification Service**: Sends notifications (e.g., email, SMS, mobile push notifications) once the payment is successfully processed, and publishes a `UserNotified` event.
- **Logistics Service**: Handles order shipping and logistics, integrates with third-party shipping services.
- **Gateway Service**: Routes requests to the appropriate microservices and provides a single entry point for the system.
- **Analytics Service**: Based on business requirements we will analyze stored data from the Warehouse databases

## 2. Technology Stack Choices

- **Java 21**
- **Kafka**: Used for event-driven communication between microservices.
- **Cassandra**: Used for cart functionality beacause it offers fast read-write operations and better availability & has cloud Compatibility.
- **Spring Boot 4.0.1**: A framework for building microservices. Java’s extensive ecosystem, paired with Spring tools like Spring Security, Spring Data, and Spring Cloud, supports large-scale systems.
- **PostgreSQL**: Used for transactional data such as orders, payments, and user data.
- **Redis**: Caches data to speed up responses, such as caching user sessions or product details.
- **Grafana**: For monitoring and alerting, using tools like Prometheus or InfluxDB to track service health and performance.
- **Docker**: Containerizes services for easy deployment. Kubernetes will be used for orchestration.
- **Azure Cloud**: Hosts the infrastructure with services like Azure Kubernetes Service (AKS), Azure Blob Storage, use Azure Load Balancer, Azure CDN, and Azure PostgreSQL.
- **Next.js**: I plan to use ISR and Next.js serves a pre-generated static page from the cache and regenerates it in the background when a specific revalidate interval is met.
- **Flutter**: For native mobile application for Android and iOs.

## 3. Communication Between Components

- **Kafka (Event-Driven)**: Microservices communicate asynchronously via Kafka events/messages.
- **Synchronous REST Calls**: Used for critical API interactions, like user authentication and order processing.

## 4. Database arhitecture
## Overview
This plan establishes a hybrid database architecture with dedicated PostgreSQL databases for critical services (Auth, Payment), shared databases for less critical services, Cassandra for cart management, and Redis for caching.

### PostgreSQL Dedicated Databases Strategy
Primary Database Instances
Read Replicas: 2 replicas per primary for user_db, product_db, order_db
Failover: Automatic failover using Azure Database for PostgreSQL with high availability
Connection Pooling: PgBouncer or Azure Connection Pooler per database cluster

1. Database: User & Auth Database (auth_db)
Purpose: User accounts, authentication, authorization, profiles
Tables: users, roles, permissions, tokens, sessions
Services: AuthService, APIService (user endpoints)
Replication: Primary + 2 Read Replicas
Isolation: Logged-in users only (no anonymous access)
Justification: Critical transactional data requiring strong consistency

2. Database: Inventory Database (product_db)
Purpose: Products, inventory, stock management, pricing
Services: Product Service, Logistics Service
Replication: Primary + 2 Read Replicas
Access: Both anonymous (read-only) and authenticated (read-write)
Justification: High read volume, write operations need consistency

3. Database Order (order_db)
Purpose: Orders, order_items, order history
Services: API Service (orders), Analytics Service
Replication: Primary + 2 Read Replicas
Access: Authenticated users only
Justification: Financial data requiring ACID guarantees

4. Payment Database (payment_db)
Tables: transactions, payment_methods, invoices, refunds, taxRates, taxRegions
Services: Payment Service
Replication: Primary + 2 Read Replicas
Justification: Financial data requires isolation, audit trails, compliance

5. Payment Database (payment_db)
Tables: notification_queue, notification_history, templates
Services: Notification Service
Replication: Primary + 1 Read Replica
Justification: Non-critical, can tolerate slight delays

### Cassandra Database Strategy for Cart
Cluster Configuration
3 -node Cassandra Cluster (Multi-Datacenter)
Keyspace: cart_keyspace
Replication Strategy: NetworkTopologyStrategy with 3 replicas (all nodes have all data)

User Separation:
1. cart_authenticated - Logged-in users
   - Tables: user_carts, cart_items, saved_for_later
   - TTL: 90 days for abandoned carts
   - No rate limiting at DB level (handled by Gateway)

2. cart_anonymous - Anonymous users
   - Tables: anonymous_carts, anonymous_cart_items
   - TTL: 30 days
   - Row-level TTL on each cart item
   - Partition by session_id with additional fingerprint validation

### Redis Caching Strategy
Application checks cache first, then DB. Updates cache after DB write and Kafka events trigger cache updates
3 Redis Instances (clustered or separate):
1. redis_cache - Application caching
   - Product catalog cache (most accessed products)
   - User session cache
   - API response cache (TO-DO investigate)
   - TTL: 5-60 minutes depending on data type

2. redis_rate_limit - Rate limiting & DDoS protection
   - IP-based rate limiting
   - User-based rate limiting
   - Device fingerprint tracking
   - Anonymous cart operation counters

3. redis_session - User sessions
   - Active user sessions
   - Token blacklist (for logout)
   - TTL: Session duration (e.g., 24 hours)
