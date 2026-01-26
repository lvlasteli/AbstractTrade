package com.example.auth.repository;

import com.example.auth.model.entity.AuthenticationEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthAuditRepository extends CrudRepository<AuthenticationEvent, UUID> {

}
