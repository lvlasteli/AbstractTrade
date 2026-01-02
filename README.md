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

## 2. Technology Stack Choices

- **Java 21**
- **Kafka**: Used for event-driven communication between microservices.
- **Spring Boot 4.0.1**: A framework for building microservices. Java’s extensive ecosystem, paired with Spring tools like Spring Security, Spring Data, and Spring Cloud, supports large-scale systems.
- **PostgreSQL**: Used for transactional data such as orders, payments, and user data.
- **Redis**: Caches data to speed up responses, such as caching user sessions or product details.
- **Grafana**: For monitoring and alerting, using tools like Prometheus or InfluxDB to track service health and performance.
- **Docker**: Containerizes services for easy deployment. Kubernetes will be used for orchestration.
- **Azure Cloud**: Hosts the infrastructure with services like Azure Kubernetes Service (AKS), Azure Blob Storage, use Azure Load Balancer, Azure CDN, and Azure PostgreSQL.
- **VueJs**: For web based frontend thats responsive for mobile.
- **Flutter**: For native mobile application for Android and iOs.

## 3. Communication Between Components

- **Kafka (Event-Driven)**: Microservices communicate asynchronously via Kafka events/messages.
- **Synchronous REST Calls**: Used for critical API interactions, like user authentication and order processing.