package com.example.auth.repository;

import com.example.auth.model.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends CrudRepository<Role, UUID> {

    Optional<Role> findByName(String name);

//    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
//    Optional<Role> findByNameWithPermissions(@Param("name") String name);
//
//    @Query("SELECT r FROM Role r WHERE r.name IN :names")
//    Set<Role> findByNameIn(@Param("names") Set<String> names);
//
//    boolean existsByName(String name);
}
