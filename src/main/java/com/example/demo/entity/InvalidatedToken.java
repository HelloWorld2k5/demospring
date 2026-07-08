package com.example.demo.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class InvalidatedToken { // Entity này là bảng lưu trữ những token đã được logout thay vì những token còn sống
    
    @Id
    String id; // lưu trữ chỉ id của token chứ không lưu token đó vì bảo toàn tính chất stateless của jwt là ko lưu trữ bất kì thông tin nào của token lên server
    Instant expirationTime; // lưu cả tg hết hạn vì, nếu có token đã đc logout mà tự hết hạn ta có thể dùng bot auto quét trong khoảng thời gian nào đó để remove token đó khỏi db cho nhẹ

}
