package com.example.auth.repository;

import com.example.auth.model.entity.Permission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long> {

//    Optional<Permission> findByName(String name);
//
//    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
//    Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);
//
//    @Query("SELECT p FROM Permission p WHERE p.resource = :resource")
//    Set<Permission> findByResource(@Param("resource") String resource);
//
//    boolean existsByName(String name);
}
