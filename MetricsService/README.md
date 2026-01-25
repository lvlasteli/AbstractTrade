# Metrics Service

## Purpose

The Metrics Service is responsible for **collecting, aggregating, and storing system metrics and operational data** from various microservices through Kafka events. It serves as the central metrics collection point for monitoring, alerting, and observability across the platform.

The service consumes metric events from Kafka topics and stores them in a time-series database for:
* **Performance monitoring** - Track system performance, latency, and throughput
* **Security metrics** - Monitor authentication events, account lockouts, and suspicious activities
* **User behavior analytics** - Track user registration, login patterns, and session management
* **Operational insights** - Provide data for dashboards, alerts, and capacity planning

The Metrics Service is fully **event-driven**, decoupled from business services, and designed to handle high-volume metric ingestion.

## Architecture

### High-Level Flow

```
Kafka Topic: auth.metrics
   ↓
Metrics Service (Kafka Consumer)
   ↓
Time-Series Database (Future: InfluxDB/TimescaleDB)
   ↓
Prometheus/Grafana (Future: Metrics Export)
```

### Event Consumption

The service consumes metric events from the `auth.metrics` Kafka topic, which includes:

* **`USER_LOGGED_IN`** - Track successful authentication events, login frequency, and user activity patterns
* **`USER_LOGGED_OUT`** - Monitor session termination and user logout patterns
* **`USER_REGISTERED`** - Track user growth, registration trends, and new user acquisition metrics
* **`ACCOUNT_LOCKED`** - Security metrics for account lockouts, failed login attempts, and threat detection
* **`IP_BLOCKED`** - Track IP-based blocking events from GatewayService, including rate limit violations and suspicious activity patterns

## Current Implementation Status

### Phase 1: Event Consumption (Current)
* Kafka consumer configured to listen to `auth.metrics` topic
* Event consumers log received messages
* **TODO**: Implement time-series database integration (InfluxDB/TimescaleDB)
* **TODO**: Implement metric aggregation and storage logic
* **TODO**: Implement Prometheus metrics export endpoint

### Phase 2: Storage (Future)
* Time-series database integration
* Metric aggregation and downsampling
* Data retention policies
* Query optimization for time-series data

### Phase 3: Export & Visualization (Future)
* Prometheus-compatible metrics endpoint
* Grafana dashboard integration
* Custom metric queries and APIs
* Alert rule configuration

## Challenges

1. **High Volume**: Authentication events can generate millions of metrics per day
   * Solution: Efficient batching and bulk inserts to time-series database
   * Downsampling for long-term storage

2. **Real-time vs Historical**: Balance between real-time metrics and historical analysis
   * Solution: Hot storage for recent data, cold storage for historical analysis

3. **Metric Types**: Different event types require different metric schemas
   * Solution: Flexible schema design and event-specific metric processors

4. **Data Retention**: Balance storage costs with retention requirements
   * Solution: Configurable retention policies and automated data archival

## Technology Stack

* **Spring Boot 4.0.1** - Application framework
* **Apache Kafka** - Event streaming platform
* **Kafka Consumer** - Consume metric events from `auth.metrics` topic
* **Future: InfluxDB/TimescaleDB** - Time-series database for metric storage
* **Future: Prometheus** - Metrics export and querying

## Event Schema

The service consumes `AuthEvent` and `GatewayEvent` objects from Kafka, which include:
* `eventId` - Unique event identifier
* `eventType` - Type of event (USER_LOGGED_IN, USER_LOGGED_OUT, IP_BLOCKED, etc.)
* `timestamp` - Event timestamp
* `sessionId` - Session identifier (if applicable)
* Event-specific fields (userId, email, ipAddress, etc.)

### IP_BLOCKED Event Fields
The `IP_BLOCKED` event (published by GatewayService) includes:
* `ipAddress` - The blocked IP address
* `reason` - Reason for blocking (e.g., "Login rate limit exceeded", "Failed login attempts exceeded")
* `blockDurationMinutes` - Duration of the IP block in minutes
* `failedAttempts` - Number of failed attempts that triggered the block
* `userAgent` - User-Agent header from the blocked request

## Monitoring & Observability

The Metrics Service itself should be monitored for:
* Consumer lag - Ensure events are being processed in a timely manner
* Processing errors - Track failed event processing
* Storage performance - Monitor database write performance
* Resource utilization - CPU, memory, and network usage

## Critical Alerts

Critical alerts should be configured for:

### Authentication & Security
* **High authentication failure rates** - Potential brute force attacks
* **Unusual authentication patterns** - Potential security incidents
* **Account lockout spikes** - Coordinated attack attempts
* **IP blocking events** - Track IP-based blocks from GatewayService for rate limit violations and suspicious activity

### Infrastructure
* **Redis connectivity issues** - Session storage unavailable
* **Database connection problems** - Cannot validate credentials
* **Session storage capacity** - Redis memory approaching limits
* **Kafka consumer lag** - Events not being processed in time

### Performance
* **High latency on authentication endpoints** - Service degradation
* **Error rate spikes** - System instability

> **Prometheus/InfluxDB** can be used for metrics collection and **Grafana dashboards** for visualization and alerting

## Summary

The Metrics Service provides a **centralized, scalable metrics collection platform** for the  system health. By consuming Kafka events and storing them in a time-series database, it enables:
* Real-time monitoring and alerting
* Historical trend analysis
* Performance optimization insights
* Security threat detection
* Capacity planning and resource optimization
