package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

// Mặc định Japrepository đã có annotation @Repository rồi nên không cần phải thêm annotation nữa
public interface UserRepository extends JpaRepository<User, String> { // String ở đây là kiểu dữ liệu của id Entity đó

    // Jpa rất hay khi cung cấp các phương thức check tồn tại theo một trường gì đó
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
