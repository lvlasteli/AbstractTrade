package com.example.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UUID userId;
    private String email;
    private String username;
    private Set<String> roles;
    private String message;
}
