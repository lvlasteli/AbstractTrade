# Quick Start Guide

Get AbstractTrade monorepo up and running

## Prerequisites

- **Docker Desktop** 24.0+ with 8GB+ RAM allocated
- **Docker Compose** 2.20+
- **Git**
- **Make** (optional but recommended and add it to path)

## Step-by-Step Setup

### 1. Clone and Navigate
```bash
git clone https://github.com/lvlasteli/AbstractTrade.git
cd AbstractTrade
```

### 2. Configure Environment
```bash
# Copy the environment template
cp env.template .env

# Edit .env and update passwords (important for security!)
# At minimum, change all passwords from *_change_me values
notepad .env  # Windows
nano .env     # Linux/Mac
```

### 3. Create Docker Network

```bash
docker network create abstracttrade-network
```

### 4. Start Everything

**Option A: Using Make (Recommended)**
```bash
make up
```

**Option A: Using Make - Only shared (Recommended)**
For local development of java applications, build only shared infrastructure
```bash
make shared-up
```

**Option B: Manual Commands (Windows PowerShell/Cmd)**
```bash
# Start shared services
docker-compose -f docker-compose.shared.yml  up -d

# Check if shared services are up and running
docker-compose -f docker-compose.shared.yml ps

# Start microservices
docker-compose -f docker-compose.services.yml up -d
# Check if microservices are up and running
docker-compose -f docker-compose.services.yml ps

# Create postgres auth database
docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/init-postgres-auth-db.sql

# Or from within the container
docker exec -it postgres-auth psql -U auth_user -d auth_db -f /scripts/init-postgres-auth-db.sql
```

**Option C: Manual Commands (Linux/Mac)**
```bash
# Start shared services
docker-compose -f docker-compose.shared.yml  up -d

# Check if shared services are up and running
docker-compose -f docker-compose.shared.yml ps

# Start microservices
docker-compose -f docker-compose.services.yml up -d
# Check if microservices are up and running
docker-compose -f docker-compose.services.yml ps

# Create postgres databases
docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/init-postgres-auth-db.sql
docker exec -i postgres-product psql -U product_user -d product_db < scripts/init-postgres-product-db.sql


Expected output: All services should show status "Up" and "healthy"
### 6. Access Services

- **API Gateway**: http://localhost:8080
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

Individual services:
- Auth Service: http://localhost:8081
- API Service: http://localhost:8082
- Product Service: http://localhost:8083
- Cart Service: http://localhost:8084
- Payment Service: http://localhost:8085
- Notification Service: http://localhost:8086
- Logistics Service: http://localhost:8087
- Analytics Service: http://localhost:8088
- Listener Service: http://localhost:8089

## Common Commands

### Using Make

```bash
make help              # Show all available commands
make up                # Start everything
make down              # Stop everything
make restart           # Restart all services
make logs              # View all logs
make logs-services     # View only microservice logs
make ps                # Show service status
```