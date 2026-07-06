package com.example.demo.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;
    String fullName;
    LocalDate dob;
    List<String> roles; // request update thêm list các roles
}
