
## Local Development - AuthService

### Running AuthService Locally with Dockerized Services

When running AuthService locally (outside Docker), it connects to dockerized Kafka and Redis services using `localhost` with exposed ports.

#### Prerequisites

1. Start the required Docker services:

   ```bash
   make shared-up
   ```
   or
   ```bash
   docker-compose -f docker-compose.shared.yml up -d kafka redis-session postgres-auth
   ```

2. Initialize the PostgreSQL database if `make shared-up` was not run:

   ```bash
   docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/init-postgres-auth-db.sql
   ```

   This creates all required tables (`users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `authentication_events`) and inserts default roles.

   **To reset the database** (drops all tables):
   ```bash
   docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/drop-postgres-auth-db.sql
   docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/init-postgres-auth-db.sql
   ```

#### Port Mappings for Local Development

The following ports are exposed on `localhost` for local development:

* **Kafka**: `localhost:29092` (PLAINTEXT_HOST listener)
* **Redis Session**: `localhost:6381` (mapped from container port 6379)
* **PostgreSQL**: `localhost:5432` (default PostgreSQL port)

#### Configuration

The `application.properties` file is configured with `localhost` defaults for local development:

```properties
# Redis Session Store
spring.data.redis.host=${REDIS_SESSION_HOST:localhost}
spring.data.redis.port=${REDIS_SESSION_PORT:6381}
spring.data.redis.password=${REDIS_SESSION_PASSWORD:session_password}

# Kafka
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
```

**Important:** If you have a `.env` file with `REDIS_SESSION_PASSWORD` set, make sure to:
- Either set the environment variable when running the app: `$env:REDIS_SESSION_PASSWORD="your_password"` (PowerShell) or `export REDIS_SESSION_PASSWORD="your_password"` (Bash)
- Or restart Redis with the default password by removing the volume: `docker-compose -f docker-compose.shared.yml down -v redis-session && docker-compose -f docker-compose.shared.yml up -d redis-session`

#### Docker Deployment

When deploying AuthService in Docker, override these values using environment variables:

```bash
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
REDIS_SESSION_HOST=redis-session
REDIS_SESSION_PORT=6379
```

This allows the service to connect using Docker network hostnames when running in containers.

#### Verifying Connections

After starting the Docker services, verify connectivity:

**Kafka:**
```bash
kafka-console-producer --bootstrap-server localhost:29092 --topic test
```

**Redis:**
```bash
# Using default password
redis-cli -h localhost -p 6381 -a session_password ping

# If you have a custom password in .env, use that instead
redis-cli -h localhost -p 6381 -a your_custom_password ping
```

**PostgreSQL:**
```bash
psql -h localhost -p 5432 -U auth_user -d auth_db
```