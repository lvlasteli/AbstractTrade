.PHONY: help shared-up infra-down services-up services-down up down restart logs clean rebuild init-cassandra wait-for-health

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

# Special function for Cassandra nodes - more lenient during cluster formation
# Allows nodes to be in "starting" or "restarting" state and gives more time for cluster joining
# Usage: $(call wait-for-cassandra-node,container-name,max-wait-seconds)
define wait-for-cassandra-node
	@echo "Waiting for $(1) to join cluster (this may take a while)..."; \
	timeout=$(2); \
	unhealthy_count=0; \
	max_unhealthy_checks=10; \
	restarting_count=0; \
	max_restarting_checks=20; \
	while [ $$timeout -gt 0 ]; do \
		state=$$(docker inspect --format='{{.State.Status}}' $(1) 2>/dev/null || echo "not-found"); \
		if [ "$$state" = "not-found" ]; then \
			echo "ERROR: $(1) container not found!"; \
			exit 1; \
		elif [ "$$state" = "restarting" ]; then \
			restarting_count=$$((restarting_count + 1)); \
			if [ $$restarting_count -gt $$max_restarting_checks ]; then \
				echo "ERROR: $(1) has been restarting for too long. Check logs: docker logs $(1)"; \
				exit 1; \
			fi; \
			echo "$(1) is restarting (check $$restarting_count/$$max_restarting_checks)..."; \
		elif [ "$$state" = "exited" ] || [ "$$state" = "dead" ]; then \
			echo "ERROR: $(1) has stopped (status: $$state). Check logs: docker logs $(1)"; \
			exit 1; \
		elif [ "$$state" = "running" ]; then \
			restarting_count=0; \
			health_status=$$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' $(1) 2>/dev/null || echo "no-healthcheck"); \
			if [ "$$health_status" = "healthy" ]; then \
				echo "$(1) is healthy and joined the cluster!"; \
				break; \
			elif [ "$$health_status" = "unhealthy" ]; then \
				unhealthy_count=$$((unhealthy_count + 1)); \
				if [ $$unhealthy_count -gt $$max_unhealthy_checks ]; then \
					echo "ERROR: $(1) has been unhealthy for too long. Check logs: docker logs $(1)"; \
					exit 1; \
				fi; \
				echo "$(1) is still joining cluster (unhealthy check $$unhealthy_count/$$max_unhealthy_checks)..."; \
			elif [ "$$health_status" = "starting" ]; then \
				echo "$(1) is starting up..."; \
			fi; \
		fi; \
		sleep 3; \
		timeout=$$((timeout - 3)); \
	done; \
	if [ $$timeout -le 0 ]; then \
		echo "ERROR: Timeout waiting for $(1) to join cluster. Check logs: docker logs $(1)"; \
		exit 1; \
	fi
endef

help:
	@echo "AbstractTrade - Docker Management Commands"
	@echo ""
	@echo "Infrastructure Commands:"
	@echo "  make shared-up - Start all infrastructure services (PostgresSQL, Redis, Cassandra, Kafka)"
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
	@$(call wait-for-container,kafka,120)
	@$(call wait-for-container,cassandra-node1,500)
	@echo "Waiting for Cassandra cluster nodes to be ready..."
	@$(call wait-for-cassandra-node,cassandra-node2,500)
	@$(call wait-for-cassandra-node,cassandra-node3,500)
	@echo "All shared services started successfully!"

infra-down:
	@echo "Stopping shared services..."
	docker-compose -f docker-compose.shared.yml down

init-cassandra:
	@echo "Waiting for Cassandra cluster to be ready..."
	@$(call wait-for-container,cassandra-node1,180)
	@echo "Creating Cassandra keyspace and tables..."
	docker exec -i cassandra-node1 cqlsh < scripts/init-cassandra.cql
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
	@echo "Stopping all shared andmicroservice containers"
	docker-compose -f docker-compose.shared.yml down
	docker-compose -f docker-compose.services.yml down

up: shared-up
	@echo "Infrastructure is healthy, proceeding with initialization..."
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


