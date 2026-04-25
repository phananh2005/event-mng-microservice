package com.sa.event_mng.repository;

import com.sa.event_mng.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(com.sa.event_mng.model.enums.Role name);
}

