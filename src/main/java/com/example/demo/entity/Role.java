package com.example.demo.entity;

import java.util.Set;

import jakarta.persistence.Entity;
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
public class Role {

    @Id
    String name; // tên role là id luôn

    String description;

    @ManyToMany // Tạo mối qh nhiều - nhiều với bảng permission
    // Tức là tạo thêm 1 bảng role_permission với 2 khoá chính của 2 bảng role và permission
    // 1 role sẽ có nhiều permissions(quyền)
    Set<Permission> permissions;
}
