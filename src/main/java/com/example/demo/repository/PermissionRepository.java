package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Permission;

// Mặc định Japrepository đã có annotation @Repository rồi nên không cần phải thêm annotation nữa
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
