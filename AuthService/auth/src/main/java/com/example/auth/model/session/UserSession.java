package com.example.auth.model.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private UUID userId;
    private String email;
    private String username;
    private Set<String> roles;
    private Set<String> permissions;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant expiresAt;
}
