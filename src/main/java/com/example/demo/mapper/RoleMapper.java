package com.example.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.request.RoleRequest;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.entity.Role;

@Mapper(componentModel = "spring") // dependency Mapstruct cho phép mapper để ánh xạ dữ liệu từ obj này sang obj khác
// Chỉ cần interface, mapper tự động sinh ra code, xem trong file class ở folder target
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true) // ignore việc map permissions từ set string sang set permission
    // ta sẽ tự map trong Role service theo cách của mình
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
