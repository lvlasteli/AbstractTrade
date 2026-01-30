# AbstractTrade - Docker Services Testing Guide

This guide provides an overview of all services, health checks, and Docker management commands. For detailed API endpoint testing, see the service-specific documentation below.

## API Documentation References

For detailed API endpoint testing and cURL commands, see:

- **Auth Service**: [AuthService/TEST_CURL_COMMANDS.md](./AuthService/TEST_CURL_COMMANDS.md)
  - User registration, login, logout
  - Session management and validation
  - Password reset flow
  - Error scenarios and validation testing

- **Product Service**: [ProductService/TEST_CURL_COMMANDS.md](./ProductService/TEST_CURL_COMMANDS.md)
  - Product listing and pagination
  - Product search
  - Category management
  - Product validation

- **Cart Service**: [CartService/TEST_CURL_COMMANDS.md](./CartService/TEST_CURL_COMMANDS.md)
  - Add/update/remove cart items
  - Anonymous and authenticated cart flows
  - Cart validation scenarios
  - Complete shopping workflows

- **Cart Validation (Gateway)**: [GatewayService/TEST_CART_VALIDATION_COMMANDS.md](./GatewayService/TEST_CART_VALIDATION_COMMANDS.md)
  - Product validation when adding to cart
  - Edge cases and error handling
  - Stock and availability testing

- **Gateway Endpoints**: [GatewayService/ENDPOINTS.md](./GatewayService/ENDPOINTS.md)
  - Complete API endpoint reference
  - Rate limiting and security

## Quick Reference: All Services and Ports

| Service | Port | Health Check | Description |
|---------|------|-------------|-------------|
| **Gateway Service** | 8080 | ✅ `/actuator/health` | API Gateway (main entry point) |
| **Auth Service** | 8081 | ✅ `/actuator/health` | User authentication & authorization |
| **API Service** | 8082 | ✅ `/actuator/health` | Core API service |
| **Product Service** | 8083 | ✅ `/actuator/health` | Product catalog management |
| **Cart Service** | 8084 | ✅ `/actuator/health` | Shopping cart operations |
| **Payment Service** | 8085 | ✅ `/actuator/health` | Payment processing |
| **Notification Service** | 8086 | ✅ `/actuator/health` | Notifications (email, SMS) |
| **Listener Service** | 8087 | ✅ `/actuator/health` | Event listener |
| **Logistics Service** | 8088 | ✅ `/actuator/health` | Order fulfillment & shipping |
| **Metrics Service** | 8089 | ✅ `/actuator/health` | Metrics aggregation |
| **Analytics Service** | 8090 | ✅ `/actuator/health` | Analytics & reporting |

### Infrastructure Services

| Service | Port | Access |
|---------|------|--------|
| **Prometheus** | 9090 | http://localhost:9090 |
| **Grafana** | 3000 | http://localhost:3000 |
| **PostgreSQL (Auth)** | 5432 | Internal only |
| **PostgreSQL (Product)** | 5433 | Internal only |
| **Redis (Session)** | 6379 | Internal only |
| **Redis (Cart)** | 6380 | Internal only |
| **Redis (Catalog)** | 6381 | Internal only |
| **Redis (Rate Limit)** | 6382 | Internal only |
| **Kafka** | 9092 | Internal only |

---

## Quick Health Check - All Services

Check if all services are running and healthy:

```bash
# Check all service health status
echo "=== Gateway Service ==="
curl -s http://localhost:8080/actuator/health | jq

echo "=== Auth Service ==="
curl -s http://localhost:8081/actuator/health | jq

echo "=== API Service ==="
curl -s http://localhost:8082/actuator/health | jq

echo "=== Product Service ==="
curl -s http://localhost:8083/actuator/health | jq

echo "=== Cart Service ==="
curl -s http://localhost:8084/actuator/health | jq

echo "=== Payment Service ==="
curl -s http://localhost:8085/actuator/health | jq

echo "=== Notification Service ==="
curl -s http://localhost:8086/actuator/health | jq

echo "=== Listener Service ==="
curl -s http://localhost:8087/actuator/health | jq

echo "=== Logistics Service ==="
curl -s http://localhost:8088/actuator/health | jq

echo "=== Metrics Service ==="
curl -s http://localhost:8089/actuator/health | jq

echo "=== Analytics Service ==="
curl -s http://localhost:8090/actuator/health | jq
```

### Simple Health Check (No jq required)

```bash
# Quick status check
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # API
curl http://localhost:8083/actuator/health  # Product
curl http://localhost:8084/actuator/health  # Cart
curl http://localhost:8085/actuator/health  # Payment
curl http://localhost:8086/actuator/health  # Notification
curl http://localhost:8087/actuator/health  # Listener
curl http://localhost:8088/actuator/health  # Logistics
curl http://localhost:8089/actuator/health  # Metrics
curl http://localhost:8090/actuator/health  # Analytics
```

### One-Liner Health Check Script

```bash
# Check all services at once
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090; do
  echo -n "Port $port: "
  curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health
  echo
done
```

---

## Quick API Testing

**Gateway URL:** `http://localhost:8080` (Recommended Entry Point)

The Gateway Service is the main entry point for all external requests. It handles:
- Request routing to appropriate microservices
- Rate limiting
- IP blocking
- Header management
- Authentication forwarding

### Quick Test Commands

For detailed endpoint testing, please refer to the service-specific documentation linked above. Here are some quick examples:

```bash
# Test Products (see ProductService/TEST_CURL_COMMANDS.md for more)
curl http://localhost:8080/products

# Test Auth (see AuthService/TEST_CURL_COMMANDS.md for more)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"testuser","password":"SecurePass123!"}'

# Test Cart (see CartService/TEST_CURL_COMMANDS.md for more)
curl -X POST http://localhost:8080/cart/items \
  -H "Content-Type: application/json" \
  -d '{"sku":"ELEC-001","quantity":2}' \
  -c cart_cookies.txt
```

### Direct Service Access (Testing Only)

For testing individual services directly (bypassing the gateway), most services require the `X-Gateway-Request` header:

```bash
# Example: Direct access to Product Service (Port 8083)
curl http://localhost:8083/products \
  -H "X-Gateway-Request: gateway-secret-change-me"
```

**Note:** See individual service documentation for complete direct access examples.

---

## Docker Container Management

### Check Running Containers

```bash
# List all containers
docker ps

# Check specific service logs
docker logs gateway-service
docker logs auth-service
docker logs product-service
docker logs cart-service

# Follow logs in real-time
docker logs -f gateway-service

# Check container stats
docker stats

# View specific service status
docker inspect gateway-service
```

### Using Make Commands

```bash
# Start all services
make up

# View all logs
make logs

# View infrastructure logs only
make logs-infra

# View microservices logs only
make logs-services

# Check status
make ps

# Stop all services
make down

# Restart services
make restart

# Rebuild and restart
make rebuild
```

---

## Complete E2E Test Flow

Here's a complete end-to-end shopping flow example. For detailed commands and variations, see the individual service documentation.

```bash
# 1. Check if services are healthy
curl http://localhost:8080/actuator/health

# 2. Register a new user (see AuthService/TEST_CURL_COMMANDS.md)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"shopper@example.com","username":"shopper1","password":"ShopSecure123!"}'

# 3. Login (see AuthService/TEST_CURL_COMMANDS.md)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"shopper@example.com","password":"ShopSecure123!"}' \
  -c session.txt -v

# 4. Browse products (see ProductService/TEST_CURL_COMMANDS.md)
curl http://localhost:8080/products -b session.txt

# 5. Search for products (see ProductService/TEST_CURL_COMMANDS.md)
curl "http://localhost:8080/products/search?q=headphones" -b session.txt

# 6. Add items to cart (see CartService/TEST_CURL_COMMANDS.md)
curl -X POST http://localhost:8080/cart/items \
  -H "Content-Type: application/json" \
  -b session.txt \
  -d '{"sku":"ELEC-001","quantity":1}'

# 7. View cart (see CartService/TEST_CURL_COMMANDS.md)
curl http://localhost:8080/cart -b session.txt

# 8. Update cart item (see CartService/TEST_CURL_COMMANDS.md)
curl -X PUT http://localhost:8080/cart/items/ELEC-001 \
  -H "Content-Type: application/json" \
  -b session.txt \
  -d '{"quantity":2}'

# 9. Proceed to checkout (if implemented)
# curl -X POST http://localhost:8080/orders/checkout -b session.txt

# 10. Logout (see AuthService/TEST_CURL_COMMANDS.md)
curl -X POST http://localhost:8080/auth/logout -b session.txt
```

**For more testing scenarios:**
- Anonymous cart flow → [CartService/TEST_CURL_COMMANDS.md](./CartService/TEST_CURL_COMMANDS.md)
- Product validation → [GatewayService/TEST_CART_VALIDATION_COMMANDS.md](./GatewayService/TEST_CART_VALIDATION_COMMANDS.md)
- Auth error testing → [AuthService/TEST_CURL_COMMANDS.md](./AuthService/TEST_CURL_COMMANDS.md)
- Product search and pagination → [ProductService/TEST_CURL_COMMANDS.md](./ProductService/TEST_CURL_COMMANDS.md)

---

## Monitoring and Observability

### Prometheus (Metrics)

```bash
# Access Prometheus UI
open http://localhost:9090

# Check targets health
curl http://localhost:9090/api/v1/targets

# Query metrics
curl "http://localhost:9090/api/v1/query?query=up"
```

### Grafana (Dashboards)

```bash
# Access Grafana UI
open http://localhost:3000

# Default credentials (if configured)
# Username: admin
# Password: admin
```

---

## Troubleshooting

### Service Not Responding

```bash
# Check if container is running
docker ps | grep <service-name>

# Check container logs for errors
docker logs <service-name>

# Check health status
curl http://localhost:<port>/actuator/health

# Restart specific service
docker restart <service-name>
```

### Database Connection Issues

```bash
# Check PostgreSQL containers
docker ps | grep postgres

# Connect to database
docker exec -it postgres-auth psql -U auth_user -d auth_db
docker exec -it postgres-product psql -U product_user -d product_db

# Check Redis
docker exec -it redis-session redis-cli ping
docker exec -it redis-cart redis-cli -a cart_password ping
```

### Network Issues

```bash
# Check if network exists
docker network ls | grep abstracttrade-network

# Inspect network
docker network inspect abstracttrade-network

# Check which containers are on the network
docker network inspect abstracttrade-network | grep Name
```


---

## Quick Start Commands

```bash
# Start everything
make up

# Check all services health
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090; do
  echo -n "Port $port: "
  curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health
  echo
done

# Test Gateway (main entry point)
curl http://localhost:8080/actuator/health

# View all logs
make logs-services

# Stop everything
make down
```

---

## Notes

- **Gateway Port 8080** is your main entry point for all API requests
- All services have health check endpoints at `/actuator/health`
- Services use **Spring Boot Actuator** for monitoring
- Authentication uses **session cookies** stored in Redis
- Cart uses **Redis** for fast access
- Products are cached in **Redis** (categories only)
- All async events go through **Kafka**
- Metrics are collected by **Prometheus** and visualized in **Grafana**

### Additional Documentation

- **Architecture**: See [ARHITECTURE.md](./ARHITECTURE.md) for system design and service interactions
- **Quick Start**: See [QUICK_START.md](./QUICK_START.md) for setup and deployment instructions
- **Gateway Endpoints**: See [GatewayService/ENDPOINTS.md](./GatewayService/ENDPOINTS.md) for complete API reference
- **Service-Specific Tests**:
  - Auth → [AuthService/TEST_CURL_COMMANDS.md](./AuthService/TEST_CURL_COMMANDS.md)
  - Products → [ProductService/TEST_CURL_COMMANDS.md](./ProductService/TEST_CURL_COMMANDS.md)
  - Cart → [CartService/TEST_CURL_COMMANDS.md](./CartService/TEST_CURL_COMMANDS.md)
  - Cart Validation → [GatewayService/TEST_CART_VALIDATION_COMMANDS.md](./GatewayService/TEST_CART_VALIDATION_COMMANDS.md)
