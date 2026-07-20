package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
public class Permission { // Đây là quyền mà các roles có thể có

    @Id
    String name; // tên quyền là id luôn

    String description;
}
