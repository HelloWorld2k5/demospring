package com.example.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;

@Mapper(componentModel = "spring") // dependency Mapstruct cho phép mapper để ánh xạ dữ liệu từ obj này sang obj khác
// Chỉ cần interface, mapper tự động sinh ra code, xem trong file class ở folder target
public interface UserMapper {
    User toUser(UserCreationRequest request); // mapper tự động ánh xạ từ request sang user, id ko có thì null
    UserResponse toUserResponse(User user); // map từ user sang user response
    void updateUser(@MappingTarget User user, UserUpdateRequest request); // mapper tự động ánh xạ từ biến request sang biến user

    // @Mapping(source = "", target = "") -> dùng để map 2 đối tượng khác tên fields, source là field cần map vào, target là field map
    // @Mapping(target = "", ignore = true) -> không map field trong target
}
