package com.sa.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.authservice.model.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
