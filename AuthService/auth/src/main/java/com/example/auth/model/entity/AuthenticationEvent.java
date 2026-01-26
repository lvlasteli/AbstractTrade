package com.example.auth.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "authentication_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo;

    @Column(name = "session_token")
    private String sessionToken;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
