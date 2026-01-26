package com.example.auth.repository;

import com.example.auth.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsernameWithRolesAndPermissions(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") UUID id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
    List<User> findAllByEmailOrUsername(@Param("email") String email, @Param("username") String username);
}
