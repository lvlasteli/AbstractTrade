# Analytics Service

## Purpose

The Analytics Service provides **business-focused analytics, reporting, and insights** for internal stakeholders (Backoffice / Admin users). It enables the business to understand performance, trends, and operational efficiency through metrics such as:

* Revenue by day, week, and month
* Orders by region, product, and channel
* Payment success and failure rates
* Refunds, taxes, and net revenue
* Logistics and fulfillment performance

The service sits on top of the analytical data warehouse and exposes **read-only, stable, and secure APIs** tailored for dashboards, BI tools, and internal consumers.

While **ClickHouse** acts as the **OLAP / data warehouse** storing large volumes of historical, immutable analytical data and performing fast aggregations, the Analytics Service focuses on:

* Translating raw analytical data into **business metrics**
* Providing a **consistent and versioned API layer**
* Enforcing access control and query governance
* Shielding the warehouse from direct external access


## Challenges

Kafka events represent low-level system activity and they are **not suitable for direct consumption by dashboards or business users** without transformation and interpretation.

Analytical schemas & bussnes needs and analytics will evolve over time and direct access to warehouse tables would tightly couple consumers to internal schemas, making changes risky and expensive. With analytics service we **decide** how we present the data and to whom.

ClickHouse is optimized for analytical performance, not for:
* Fine-grained authorization
* Rate limiting
* Public exposure (it needs to be in the private network)

Uncontrolled access might lead to:
* Data leakage of financial or sensitive data
* Expensive or poorly written queries


## Solution

### High-Level Architecture

```
Kafka Topics
   ↓
ClickHouse (OLAP / Warehouse)
   ↓
Analytics Service (Read-only APIs)
   ↓
Backoffice UI / BI Tools / Internal Consumers
```

### Data Ingestion and Storage

**Kafka**
* Serves as the central event bus
* Retains events long enough for replay
* Uses Schema Registry to manage schema evolution

**ClickHouse**
* Consumes Kafka events directly using Kafka Engine tables
* Stores data in:
  * Raw event tables (append-only)
  * Transformed fact tables via materialized views
  * Dimension tables for descriptive attributes
* Uses ReplicatedMergeTree tables
* Partitions data primarily by date for efficient querying and retention
* Scales horizontally using shards


### Analytics Service Responsibilities

The Analytics Service acts as the **business and access layer** on top of the warehouse:
* Exposes **REST APIs** for analytical queries (e.g. revenue, orders, conversion rates)
* Encapsulates **business logic and metric definitions**
* Provides **API versioning** to support backward compatibility
* Applies **authorization and role-based access control** for Backoffice users
* Protects the warehouse from unbounded or unsafe queries (no updates or deletes)
* Stateless and horizontally scalable
* (TO-DO is it worth it?) Optionally caches frequently accessed results using Redis

The service is strictly **read-only** and never modifies analytical data.

### Monitoring and Observability

Monitoring is critical to ensure reliability and performance of analytics workloads.

1. **ClickHouse Monitoring**
    * Query latency and throughput
    * Slow query logs
    * CPU, memory, and disk IO usage
    * Replication and shard health

2. **Kafka Monitoring**
    * Consumer lag for ClickHouse ingestion
    * Topic throughput and retention

3. **Analytics Service Monitoring**
    * API latency and error rates
    * Request volume per endpoint

> Prometheus/InfluxDb can be used for metrics collection and Grafana dashboards for visualization and alerting


## Summary

The Analytics Service provides a **controlled, business-oriented interface** to analytical data stored in ClickHouse. By combining Kafka-based event ingestion, a scalable OLAP warehouse, and a dedicated analytics API layer, the system achieves:

* Clear separation between transactional and analytical workloads
* Consistent and trustworthy business metrics
* Secure and governed access for Backoffice users
* High-performance analytics that scale independently of core services

This design ensures the analytics platform remains **scalable, maintainable, and aligned with evolving business requirements**.
