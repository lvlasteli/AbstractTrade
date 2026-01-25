package com.example.auth.service;

import com.example.auth.event.AuthEventPublisher;
import com.example.auth.event.schema.UserRegisteredEvent;
import com.example.auth.model.dto.request.RegisterRequest;
import com.example.auth.model.dto.request.RequestMetadata;
import com.example.auth.model.dto.response.AuthResponse;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.model.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserService userService;
    private final SessionService sessionService;
    private final AuditService auditService;
    private final AuthEventPublisher eventPublisher;
    private final UserSessionFactory sessionFactory;

    @Transactional
    public AuthResponse register(RegisterRequest request, RequestMetadata metadata) {
        User user = userService.createUser(request);

        auditService.logRegister(user.getId(), metadata);

        UserSession session = sessionFactory.create(user, metadata);
        String sessionId = sessionService.createSession(session);

        eventPublisher.publish(UserRegisteredEvent.create(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                roleNames(user),
                sessionId
        ));

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roleNames(user))
                .message("Registration successful")
                .build();
    }

    private Set<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
