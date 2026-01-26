package com.example.auth.service;

import com.example.auth.constants.ErrorMsg;
import com.example.auth.exception.UserAlreadyExistsException;
import com.example.auth.model.dto.request.RegisterRequest;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(RegisterRequest request) {
        List<User> existingUsers = userRepository.findAllByEmailOrUsername(request.getEmail(), request.getUsername());
        if (!existingUsers.isEmpty()) {
            throw new UserAlreadyExistsException(String.format(ErrorMsg.EMAIL_ALREADY_REGISTERED, request.getEmail()));
        }


        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException(String.format(ErrorMsg.DEFAULT_ROLE_NOT_FOUND, DEFAULT_ROLE)));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user: {} with email: {}", savedUser.getUsername(), savedUser.getEmail());

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmailOrUsernameWithRoles(String identifier) {
        return userRepository.findByEmailOrUsernameWithRolesAndPermissions(identifier);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByIdWithRoles(UUID id) {
        return userRepository.findByIdWithRolesAndPermissions(id);
    }

    @Transactional
    public void resetUserAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public void lockAccount(User user) {
        user.setIsLocked(true);
        userRepository.save(user);
        log.warn("Account locked for user: {}", user.getEmail());
    }

    @Transactional
    public void unlockAccount(User user) {
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("Account unlocked for user: {}", user.getEmail());
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
