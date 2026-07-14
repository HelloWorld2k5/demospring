package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.PermissionRequest;
import com.example.demo.dto.response.PermissionResponse;
import com.example.demo.entity.Permission;
import com.example.demo.mapper.PermissionMapper;
import com.example.demo.repository.PermissionRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@Data
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    // Tạo quyền
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);

        if (permission == null) return null;

        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    // Lấy tất cả các quyền
    public List<PermissionResponse> getAll() {
        List<Permission> permissions = permissionRepository.findAll();

        return permissions.stream()
                .map(permission -> permissionMapper.toPermissionResponse(permission))
                .toList();
    }

    // Xoá quyền theo name
    public void delete(String permissionName) {
        if (permissionName != null) {
            permissionRepository.deleteById(permissionName);
        }
    }
}
