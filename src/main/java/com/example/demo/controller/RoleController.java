package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.RoleRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.service.RoleService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/roles") // định dạng chung cho đường dẫn
@Data
@RequiredArgsConstructor // chỉ tạo constructor chỉ các fields có final hoặc @NonNull
public class RoleController {

    private final RoleService roleService;
    
    @PostMapping
    public ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {

        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll() {

        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }
    
    @DeleteMapping("/{roleName}")
    public ApiResponse<Void> delete(@PathVariable String roleName) {
        roleService.delete(roleName);
        
        return ApiResponse.<Void>builder().build();
    }

}
