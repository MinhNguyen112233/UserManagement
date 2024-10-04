package com.example.practice_spring_boot.repository;

import com.example.practice_spring_boot.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
