package com.example.demo.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.RoleRequest;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@Data
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    // Tạo role
    public RoleResponse create(RoleRequest request) {
        Role role = roleMapper.toRole(request);

        // Lấy mảng tên string các quyền từ request, rồi tìm tất cả các quyền theo id, trả về list các quyền
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions)); // set list quyền đó vào trong role (ta ko set trong mapper là để tự set ngoài này)

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    // Lấy tất cả các roles
    public List<RoleResponse> getAll() {
        List<Role> roles = roleRepository.findAll();

        return roles
                .stream()
                .map(role -> roleMapper.toRoleResponse(role)).toList();
    }

    // xoá role theo tên (tên chính là id)
    public void delete(String roleName) {
        if (roleName != null) {
            roleRepository.deleteById(roleName);
        }
    }

}
