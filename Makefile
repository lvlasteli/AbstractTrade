SHELL := bash

.PHONY: help shared-up shared-down services-up all-down up down restart logs clean rebuild wait-for-health kafka-topics kafka-topics-delete

# Helper function to wait for a single container to be healthy
# Usage: $(call wait-for-container,container-name,max-wait-seconds)
# If container has health check, waits for healthy status
# If no health check, waits for container to be running
define wait-for-container
	@echo "Waiting for $(1) to be ready..."; \
	timeout=$(2); \
	while [ $$timeout -gt 0 ]; do \
		health_status=$$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' $(1) 2>/dev/null || echo "not-found"); \
		if [ "$$health_status" != "no-healthcheck" ] && [ "$$health_status" != "not-found" ]; then \
			if [ "$$health_status" = "healthy" ]; then \
				echo "$(1) is healthy!"; \
				break; \
			elif [ "$$health_status" = "unhealthy" ]; then \
				echo "ERROR: $(1) is unhealthy!"; \
				exit 1; \
			fi; \
		else \
			state=$$(docker inspect --format='{{.State.Status}}' $(1) 2>/dev/null || echo "not-found"); \
			if [ "$$state" = "running" ]; then \
				echo "$(1) is running!"; \
				break; \
			fi; \
		fi; \
		sleep 2; \
		timeout=$$((timeout - 2)); \
	done; \
	if [ $$timeout -le 0 ]; then \
		echo "ERROR: Timeout waiting for $(1) to become ready"; \
		exit 1; \
	fi
endef

help:
	@echo "AbstractTrade - Docker Management Commands"
	@echo ""
	@echo "Infrastructure Commands:"
	@echo "  make shared-up - Start all infrastructure services (PostgresSQL, Redis, Kafka)"
	@echo "  make shared-down - Stop all infrastructure services"
	@echo "  make init-postgres-product-db - Initialize postgres product database"
	@echo "  make init-postgres-auth-db - Initialize postgres auth database"
	@echo "  make drop-postgres-product-db - Drop postgres product database"
	@echo "  make drop-postgres-auth-db - Drop postgres auth database"
	# @echo "  make init-postgres-order-db - Initialize postgres order database"
	# @echo "  make init-postgres-payment-db - Initialize postgres payment database"
	# @echo "  make init-postgres-notification-db - Initialize postgres notification database"
	@echo "  make kafka-topics - Create Kafka topics (auth_notifications, auth_metrics)"
	@echo "  make kafka-topics-delete - Delete Kafka topics (auth_notifications, auth_metrics)"
	@echo ""
	@echo "Microservices Commands:"
	@echo "  make services-up"
	@echo "  make all-down"
	@echo "  make services-build"
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

shared-up:
	@echo "Starting shared services..."
	docker-compose -f docker-compose.shared.yml up -d
	@echo "Waiting for shared services to be healthy..."
	@$(call wait-for-container,postgres-auth,120)
	@$(call wait-for-container,postgres-product,120)
	@$(call wait-for-container,postgres-order,120)
	@$(call wait-for-container,postgres-payment,120)
	@$(call wait-for-container,postgres-notification,120)
	@$(call wait-for-container,redis-catalog,120)
	@$(call wait-for-container,redis-rate-limit,120)
	@$(call wait-for-container,redis-session,120)
	@$(call wait-for-container,redis-cart,120)
	@$(call wait-for-container,kafka,120)
	@echo "Creating Kafka topics..."
	@make kafka-topics
	@echo "All shared services started successfully!"
	@echo "Initialize the postgres auth database"
	@make init-postgres-auth-db
	@make init-postgres-product-db
	@echo "Postgres dbs are initialized successfully!"


shared-down:
	@echo "Stopping shared services..."
	docker-compose -f docker-compose.shared.yml down


init-postgres-auth-db:
	@echo "Initializing postgres auth database..."
	docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/init-postgres-auth-db.sql
	@echo "Postgres auth database initialized successfully!"


init-postgres-product-db:
	@echo "Initialize the postgres product database"
	docker exec -i postgres-product psql -U product_user -d product_db < scripts/init-postgres-product-db.sql
	@echo "Postgres product database initialized successfully!"

drop-postgres-auth-db:
	@echo "Dropping postgres auth database..."
	docker exec -i postgres-auth psql -U auth_user -d auth_db < scripts/drop-postgres-auth-db.sql
	@echo "Postgres auth database dropped successfully!"

drop-postgres-product-db:
	@echo "Dropping postgres product database..."
	docker exec -i postgres-product psql -U product_user -d product_db < scripts/drop-postgres-product-db.sql
	@echo "Postgres product database dropped successfully!"

kafka-topics:
	@echo "Waiting for Kafka to be ready..."
	@$(call wait-for-container,kafka,120)
	@bash scripts/create-kafka-topics.sh

kafka-topics-delete:
	@echo "Waiting for Kafka to be ready..."
	@$(call wait-for-container,kafka,120)
	@bash scripts/delete-kafka-topics.sh

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
	@echo "Microservices stopped successfully!"

all-down:
	@echo "Stopping all shared andmicroservice containers"
	docker-compose -f docker-compose.shared.yml down
	docker-compose -f docker-compose.services.yml down

up: shared-up
	@echo "Infrastructure is healthy, proceeding with initialization..."
	@make services-up
	@echo ""
	@echo "All services are up and running!"
	@echo "Gateway: http://localhost:8080"
	@echo "Grafana: http://localhost:3000"

down: all-down
	@echo "All services stopped!"

restart: down up

rebuild:
	@echo "Rebuilding all services..."
	docker-compose build
	docker-compose -f docker-compose.services.yml build
	@make restart

logs:
	docker-compose logs -f docker-compose.services.yml

logs-infra:
	docker-compose logs -f docker-compose.shared.yml

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


