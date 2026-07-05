package com.example.demo.mapper;

import org.mapstruct.Mapper;

import com.example.demo.dto.request.PermissionRequest;
import com.example.demo.dto.response.PermissionResponse;
import com.example.demo.entity.Permission;

@Mapper(componentModel = "spring") // dependency Mapstruct cho phép mapper để ánh xạ dữ liệu từ obj này sang obj khác
// Chỉ cần interface, mapper tự động sinh ra code, xem trong file class ở folder target
public interface PermissionMapper {

    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);

}
