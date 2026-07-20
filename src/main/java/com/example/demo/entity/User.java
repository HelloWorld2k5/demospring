package com.example.demo.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter // Không nên dùng @Data cho entity, bởi nó sẽ tự động có các method như hashCode và equals
@Setter // gây ra các lỗi tiềm ẩn như LazyInitializationException, StackOverflowError
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String userId;

    @Column(
        name = "username",
        unique = true, // cột username unique (chỉ có 1)
        columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci" // username không phân biệt chữ hoa và thường
    )
    String username;
    String password;
    String fullName;
    LocalDate dob;

    @ManyToMany
    Set<Role> roles; // 1 user có thể có nhiều roles
}
