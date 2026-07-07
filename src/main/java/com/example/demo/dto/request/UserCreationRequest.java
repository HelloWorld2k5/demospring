package com.example.demo.dto.request;

import java.time.LocalDate;

import com.example.demo.validator.DobConstraint;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
// import lombok.Getter;
import lombok.NoArgsConstructor;
// import lombok.Setter;
import lombok.experimental.FieldDefaults;

// @Getter // dependency lombok giúp tạo các getter cho các fields
// @Setter // lombok giúp tạo các setter cho các fields
@Data // tự động thêm getter, setter, constructor, tostring
@Builder // thêm tính năng builder (builder pattern) cho phép tạo nhanh UserCreationRequest
// Vd: UserCreationRequest ucr1 = UserCreationRequest.builder().username("Truong").password("123").build()
@NoArgsConstructor // tự tạo constructor ko tham số
@AllArgsConstructor // tự tạo constructor đủ tham số
@FieldDefaults(level = AccessLevel.PRIVATE) // tự động set access modifier cho fields là private

public class UserCreationRequest {

    String username;

    @Size(min = 8, message = "PASSWORD_INVALID") // msg trả về là enum key, trong GlobalException sẽ lấy msg ra và response
    String password;
    String fullName;

    @DobConstraint(min = 18, message = "INVALID_DOB") // msg trả về chính là enum key
    LocalDate dob;
}
