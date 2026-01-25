package com.example.auth.service;

import com.example.auth.model.dto.request.RequestMetadata;
import com.example.auth.model.entity.Permission;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.model.session.UserSession;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserSessionFactory {

    public UserSession create(User user, RequestMetadata metadata) {
        return UserSession.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roleNames(user))
                .permissions(permissionNames(user))
                .ipAddress(metadata.ipAddress())
                .userAgent(metadata.userAgent())
                .build();
    }

    private Set<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> permissionNames(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
