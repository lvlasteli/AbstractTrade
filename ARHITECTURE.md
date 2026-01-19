# AbstractTrade - Docker Architecture Visualization

## System Overview

### 1. Gateway Service (Port 8080)
- API Gateway and routing
- Single entry point for all client requests
- Load balancing and request forwarding

### 2. Auth Service (Port 8081)
- JWT authentication
- OAuth2 integration
- Role-based access control (RBAC)
- Session management

### 3. API Service (Port 8082)
- Public and protected REST endpoints
- User data management
- Order processing coordination

### 4. Product Service (Port 8083)
- Product catalog management
- Inventory and stock control
- Price calculations
- Publishes `OrderProcessed` events

### 5. Cart Service (Port 8084)
- Shopping cart operations
- Supports both authenticated and anonymous users
- Uses Cassandra for high availability
- Rate limiting integration

### 6. Payment Service (Port 8085)
- Payment processing
- Integration with Porezna SOAP API (tax)
- Listens to `OrderProcessed` events
- Publishes `PaymentCompleted` events

### 7. Notification Service (Port 8086)
- Email, SMS, push notifications
- Listens to `PaymentCompleted` events
- Notification queue management
- Template-based messaging

### 8. Logistics Service (Port 8087)
- Order fulfillment
- Shipping integration
- Third-party logistics providers
- Delivery tracking

### 9. Analytics Service (Port 8088)
- Business intelligence
- Data warehouse operations
- Reporting and insights
- Event stream processing

### 10. Listener Service (Port 8089)
- Third-party webhook handling
- Event parsing and validation
- Kafka topic publishing

## Service Communication Patterns

### 1. Synchronous Communication (REST)
```
Client ──► Gateway ──► Auth Service ──► PostgreSQL (auth_db)
              │
              ├──► API Service ──► PostgreSQL (auth_db, order_db)
              │
              ├──► Product Service ──► PostgreSQL (product_db)
              │                    └──► Redis (cache)
              │
              └──► Cart Service ──► Cassandra (cart_keyspace)
                                └──► Redis (rate_limit)
```

### 2. Asynchronous Communication (Kafka Events)
```
Product Service ──► Kafka Topic: "order.processed"
                         │
                         ├──► Payment Service
                         │         │
                         │         └──► Kafka Topic: "payment.completed"
                         │                   │
                         │                   ├──► Notification Service
                         │                   │         │
                         │                   │         └──► Kafka: "user.notified"
                         │                   │
                         │                   └──► Logistics Service
                         │
                         └──► Analytics Service
```

### 3. Data Flow Example: Order Processing
```
1. Client Request
   └─► Gateway :8080
       └─► API Service :8082
           ├─► PostgreSQL (order_db) - Create order
           └─► Kafka - Publish "order.created"

2. Event Processing
   └─► Product Service (listens to Kafka)
       ├─► PostgreSQL (product_db) - Check stock
       ├─► Redis (cache) - Update product cache
       └─► Kafka - Publish "order.processed"

3. Payment Processing
   └─► Payment Service (listens to Kafka)
       ├─► PostgreSQL (payment_db) - Process payment
       └─► Kafka - Publish "payment.completed"

4. Notification
   └─► Notification Service (listens to Kafka)
       ├─► PostgreSQL (notification_db) - Log notification
       └─► Send email/SMS to user

5. Logistics
   └─► Logistics Service (listens to Kafka)
       └─► PostgreSQL (product_db) - Update shipping status
```

## Database Allocation

| Database | Services | Purpose | Pg-Port    |
|----------|----------|---------|---------|
| `auth_db` (PostgreSQL) | Auth Service, API Service | Users, roles, permissions, sessions | 5432 |
| `product_db` (PostgreSQL) | Product Service, Logistics Service | Products, inventory, stock, shipping | 5433 |
| `order_db` (PostgreSQL) | API Service, Analytics Service | Orders, order history, analytics | 5434 |
| `payment_db` (PostgreSQL) | Payment Service | Transactions, invoices, tax data | 5435 |
| `notification_db` (PostgreSQL) | Notification Service | Notification queue, history, templates | 5436 |
| `cart_keyspace` (Cassandra) | Cart Service | User carts, anonymous carts (with TTL) | / |

## Redis Instance Allocation

| Redis Instance | Purpose | Eviction Policy | Max Memory |
|----------------|---------|-----------------|------------|
| `redis-cache` :6379 | Product catalog, API responses | allkeys-lru | 512MB |
| `redis-ratelimit` :6380 | Rate limiting, DDoS protection, Device fingerprinting | volatile-ttl | 256MB |
| `redis-session` :6381 | User sessions, token blacklist | volatile-lru | 512MB |

## Cassandra Cluster Strategy

- **3-Node Cluster**: Full replication across all nodes
- **Replication Factor**: 3 (all nodes have all data)
- **Consistency Level**: QUORUM (2 of 3 nodes must respond)
- **Keyspace**: `cart_keyspace`
- **Tables**:
  - `user_carts` - Authenticated user carts (90-day TTL)
  - `cart_items` - Cart item details
  - `anon_carts` - Guest user carts (30-day TTL)
  - `anon_cart_items` - Guest cart items (30-day TTL)

## Port Mapping Reference

### Microservices
- Gateway: 8080
- Auth: 8081
- API: 8082
- Product: 8083
- Cart: 8084
- Payment: 8085
- Notification: 8086
- Logistics: 8087
- Analytics: 8088
- Listener: 8089

### Databases
- PostgreSQL (Auth): 5432
- PostgreSQL (Product): 5433
- PostgreSQL (Order): 5434
- PostgreSQL (Payment): 5435
- PostgreSQL (Notification): 5436
- Cassandra Node 1: 9042
- Cassandra Node 2: 9043
- Cassandra Node 3: 9044

### Caching & Messaging
- Redis (Cache): 6379
- Redis (Rate Limit): 6380
- Redis (Session): 6381
- Kafka: 9092

### Monitoring
- Prometheus: 9090
- Grafana: 3000

### Development Tools (dev mode)
- pgAdmin: 5050
- Redis Commander: 8081
- Kafka UI: 8090

## Startup Sequence

```
1. Network Creation
   └─► abstracttrade-network (Docker bridge network)

2. Core Infrastructure (Parallel Start)
   ├─► PostgreSQL (all 5 instances)
   ├─► Redis (all 3 instances)
   └─► Kafka (KRaft mode)

3. Dependent Infrastructure
   └─► Cassandra Node 1

4. Cassandra Cluster Formation
   ├─► Cassandra Node 2 (depends on: Node 1)
   └─► Cassandra Node 3 (depends on: Node 1)

5. Cassandra Initialization
   └─► Create keyspace and tables

6. Monitoring Stack
   ├─► Prometheus
   └─► Grafana (depends on: Prometheus)

7. Microservices (Parallel Start with Health Checks)
   ├─► Auth Service (depends on: postgres-auth, redis-session, kafka)
   ├─► API Service (depends on: postgres-auth, postgres-order, redis-cache, kafka)
   ├─► Product Service (depends on: postgres-product, redis-cache, kafka)
   ├─► Cart Service (depends on: cassandra-*, redis-ratelimit, kafka)
   ├─► Payment Service (depends on: postgres-payment, kafka)
   ├─► Notification Service (depends on: postgres-notification, kafka)
   ├─► Logistics Service (depends on: postgres-product, kafka)
   ├─► Analytics Service (depends on: postgres-order, kafka)
   ├─► Listener Service (depends on: kafka)
   └─► Gateway Service (depends on: auth-service, api-service)
```

## Health Check Strategy

All services implement health checks:

### Infrastructure Services
- **PostgreSQL**: `pg_isready` check every 10s
- **Redis**: Connection test every 10s
- **Cassandra**: CQL query test every 30s (60s startup period)
- **Kafka**: Broker API version check every 10s (30s startup period)

### Microservices
- **Endpoint**: `/actuator/health` (Spring Boot Actuator)
- **Interval**: 30 seconds
- **Timeout**: 10 seconds
- **Startup Period**: 40-60 seconds
- **Retries**: 3

## Environment Configuration

See `env.template` for all configuration options:
- Database credentials (per database)
- Redis passwords (per instance)
- Cassandra cluster settings
- JWT secrets
- Monitoring credentials
- External API keys

## Production Migration Path

Development (Docker Compose) → Production (Kubernetes/AKS):

| Component | Development | Production (Azure) |
|-----------|-------------|-------------------|
| Microservices | Docker Containers | AKS Pods |
| PostgreSQL | Docker Containers | Azure Database for PostgreSQL |
| Redis | Docker Containers | Azure Cache for Redis |
| Cassandra | Docker Containers | Azure Cosmos DB (Cassandra API) |
| Kafka | Docker Container | Azure Event Hubs |
| Monitoring | Prometheus + Grafana | Azure Monitor + Prometheus |
| Secrets | .env file | Azure Key Vault |
| Load Balancing | Gateway Service | Azure Load Balancer |
| Container Registry | Local | Azure Container Registry |

