# Notification Service

## Purpose

The Notification Service is responsible for **delivering user-facing communications** across multiple channels in response to system events and business workflows. It consumes events from Kafka and sends notifications via:

* **Email** (transactional and marketing)
* **SMS** (critical alerts, OTPs, reminders)
* **Mobile Push Notifications**
  * Android (Firebase Cloud Messaging – FCM)
  * iOS (Apple Push Notification Service – APNs)

The service supports multiple notification categories, including:
* **Transactional notifications** (order confirmation, payment success/failure, shipping updates)
* **Marketing notifications** (campaigns, promotions, product announcements)
* **Reminders and alerts** (abandoned carts, subscription renewals, delivery reminders)

The Notification Service is fully **event-driven**, scalable, and decoupled from core business services.


## Challenges

1. Different channels have different characteristics:
    * Email: high throughput, eventual delivery
    * SMS: low latency, higher cost
    * Push notifications: device- and platform-dependent

2. Notifications must:
    * Avoid multiple deliveries (spam)
    * Handle 3rd party provider failures gracefully
    * Support retries and delayed delivery

3. comply with user preferences and consent meaning:
    * Users can opt-out of marketing notifications
    * We might have different languages for differnet users
    * Users might prefer email over the sms
    * Regional regulatory requirements must be respected

4. Delivery confirmation monitoring:
    * Need to verify that messages are successfully delivered via webhooks or provider responses (ListenerService passes messages in kafka)
    * Detect and alert on failures or delays

## Solution

### High-Level Architecture

```
Kafka Topics
   ↓
Notification Service
   ├── Email Provider
   ├── SMS Provider
   └── Push Providers (FCM / APNs)
```

The Notification Service consumes domain events, evaluates notification rules, and delivers messages through the appropriate channels.

### Events Consumed from Auth Service

The Notification Service consumes notification events from the `auth_notifications` Kafka topic, which includes:

* **`PASSWORD_RESET_REQUESTED`** - User requests password reset (forgot password). Service should send reset email with reset token.
* **`ACCOUNT_LOCKED`** - User account has been locked due to security reasons (e.g., too many failed login attempts). Service should send security alert email to user.
* **`USER_REGISTERED`** - New user has registered. Service should send welcome email.
* **`PASSWORD_CHANGED`** - User has successfully changed their password. Service should send confirmation email.

These events are published by the Auth Service and consumed by the Notification Service for user-facing communications.


### Delivery and Retry Strategy

* Provider-specific retry policies
* Exponential backoff for transient failures
* Dead-letter topics for failed notifications
* Idempotency keys to prevent duplicates

Transactional notifications are prioritized over marketing messages.

### Persistence Model 
The service maintains its own database for state and audit for:
* Notification queue and state
* Delivery attempts and status
* User notification preferences
* Templates and metadata

### Monitoring & Observability
To ensure reliability and track successful delivery:

1. Webhook confirmation tracking
    * Listener service sends delivery confirmations back to Notification Service
    * Store status updates in the database (delivered, failed, pending)

Grafana dashboards should track:
    * Notification success rate per channel – delivered vs failed
    * Retry attempts and dead-letter counts – number of retries and failures
    * Pending deliveries – messages stuck in queue or awaiting webhook confirmation. We should alert on sudden increases in pending deliveries
    * Provider error rates – track third-party service errors or throttling. We should alert any higgh failure rates in any channel

## Infrastructure Recommendations

### Core Components

* **Security**
    * Secrets  for configs stored in secure vaults
    * Provider credentials rotated regularly
    * PII handled carefully in logs and payloads
    * Listener will recive webhooks from 3rd party providers

* **3rd party Providers**
  * Email: BlueBird, SendGrid, or equivalent
  * SMS: Twilio or Snitch
  * Push: FCM (Android), APNs (iOS)

## Summary

The Notification Service provides a **centralized, event-driven communication platform** for the system. By consuming Kafka events and delivering notifications across email, SMS, and mobile push channels, it enables:
* Reliable user communication
* Consistent messaging across channels
* Scalable fan-out of notifications
* Enforcement of user preferences and compliance rules
* Monitoring of delivery success and failures through webhook confirmation tracking