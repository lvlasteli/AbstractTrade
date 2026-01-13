.PHONY: help infra-up infra-down services-up services-down up down restart logs clean rebuild init-cassandra

help:
	@echo "AbstractTrade - Docker Management Commands"
	@echo ""
	@echo "Infrastructure Commands:"
	@echo "  make infra-up - Start all infrastructure services (PostgreSQL, Redis, Cassandra, Kafka)"
	@echo "  make infra-down - Stop all infrastructure services"
	@echo "  make init-cassandra - Initialize Cassandra keyspace and tables"
	# @echo "  make init-postgres-product-db - Initialize postgres product database"
	@echo "  make init-postgres-auth-db - Initialize postgres auth database"
	# @echo "  make init-postgres-order-db - Initialize postgres order database"
	# @echo "  make init-postgres-payment-db - Initialize postgres payment database"
	# @echo "  make init-postgres-notification-db - Initialize postgres notification database"
	@echo ""
	@echo "Microservices Commands:"
	@echo "  make services-up"
	@echo "  make services-down"
	@echo "  make services-build
	@echo ""
	@echo "Combined Commands:"
	@echo "  make up               - Start infrastructure, wait, then start services"
	@echo "  make down             - Stop everything"
	@echo "  make restart          - Restart everything"
	@echo "  make rebuild          - Rebuild and restart everything"
	@echo ""
	@echo "Utility Commands:"
	@echo "  make logs             - View logs from all services"
	@echo "  make logs-infra       - View logs from infrastructure services only"
	@echo "  make logs-services    - View logs from microservices only"
	@echo "  make clean            - Remove all containers, volumes, and networks"
	@echo "  make ps               - Show status of all containers"
	@echo ""

infra-up:
	@echo "Starting shared services..."
	docker-compose up -f docker-compose.shared.yml -d
	@echo "Waiting for shared services to be healthy..."
	@sleep 10
	@echo "All shared services started successfully!"

infra-down:
	@echo "Stopping shared services..."
	docker-compose -f docker-compose.shared.yml down

init-cassandra:
	@echo "Waiting for Cassandra cluster to be ready..."
	@sleep 30
	@echo "Creating Cassandra keyspace and tables..."
	docker exec -it cassandra-node1 cqlsh -e "\
		CREATE KEYSPACE IF NOT EXISTS cart_keyspace \
		WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1': 3} AND durable_writes = true; \
		\
		USE cart_keyspace; \
		\
		CREATE TABLE IF NOT EXISTS user_carts ( \
			user_id UUID, \
			cart_id UUID, \
			created_at TIMESTAMP, \
			updated_at TIMESTAMP, \
			status TEXT, \
			PRIMARY KEY (user_id, cart_id) \
			WITH CLUSTERING ORDER BY (cart_id DESC) \
			AND default_time_to_live = 7776000 \
			AND comment = 'Main cart records for authenticated users'; \
		); \
		\
		CREATE TABLE IF NOT EXISTS user_cart_items ( \
			cart_id UUID, \
			product_id UUID, \
			quantity INT, \
			price DECIMAL, \
			added_at TIMESTAMP, \
			updated_at TIMESTAMP, \
			PRIMARY KEY (cart_id, product_id) \
			WITH CLUSTERING ORDER BY (product_id ASC) \
			AND comment = 'Individual items in authenticated user carts'; \
		); \
		\
		CREATE TABLE IF NOT EXISTS anon_carts ( \
			session_id TEXT, \
			cart_id UUID, \
			fingerprint TEXT, \
			created_at TIMESTAMP, \
			updated_at TIMESTAMP, \
			ip_address TEXT, \
			PRIMARY KEY (session_id, cart_id) \
			WITH CLUSTERING ORDER BY (cart_id DESC) \
			AND default_time_to_live = 2592000 \
			AND comment = 'Cart records for anon/guest users'; \
		\
		CREATE TABLE IF NOT EXISTS anon_cart_items ( \
			cart_id UUID, \
			product_id UUID, \
			quantity INT, \
			price DECIMAL, \
			added_at TIMESTAMP, \
			PRIMARY KEY (cart_id, product_id) \
			WITH CLUSTERING ORDER BY (product_id ASC) \
			AND default_time_to_live = 2592000 \
			AND comment = 'Individual items in anonymous user carts'; \
		); \
	@echo "Cassandra initialization complete!"

init-postgres-auth-db:
	@echo "Initializing postgres auth database..."
	docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/init-postgres-auth-db.sql
	@echo "Postgres auth database initialized successfully!"

services-build:
	@echo "Building all microservices..."
	docker-compose -f docker-compose.services.yml build

services-up:
	@echo "Starting microservices..."
	docker-compose -f docker-compose.services.yml up -d
	@echo "Microservices started successfully!"

services-down:
	@echo "Stopping microservices..."
	docker-compose -f docker-compose.services.yml down

up: infra-up
	@echo "Waiting for infrastructure to stabilize..."
	@sleep 20
	@make init-cassandra
	@make services-up
	@echo ""
	@echo "All services are up and running!"
	@echo "Gateway: http://localhost:8080"
	@echo "Grafana: http://localhost:3000"

down: services-down infra-down
	@echo "All services stopped!"

restart: down up

rebuild:
	@echo "Rebuilding all services..."
	docker-compose build
	docker-compose -f docker-compose.services.yml build
	@make restart

logs:
	docker-compose logs -f & docker-compose -f docker-compose.services.yml logs -f

logs-infra:
	docker-compose logs -f

logs-services:
	docker-compose -f docker-compose.services.yml logs -f

ps:
	@echo "Shared Services:"
	@docker-compose -f docker-compose.shared.yml ps
	@echo ""
	@echo "Microservices:"
	@docker-compose -f docker-compose.services.yml ps

# Cleanup
clean:
	@echo "WARNING: This will remove all containers, volumes, and networks!"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose -f docker-compose.shared.yml down -v; \
		docker-compose -f docker-compose.services.yml down -v; \
		docker network rm abstracttrade-network 2>/dev/null || true; \
		echo "Cleanup complete!"; \
	fi


