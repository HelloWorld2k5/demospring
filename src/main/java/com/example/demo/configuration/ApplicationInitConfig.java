package com.example.demo.configuration;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Config lần đầu app chạy thì sẽ tạo 1 user admin duy nhất
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.username}")
    private String defaultAdminUsername;

    @Value("${app.init.admin.password}")
    private String defaultAdminPassword;

    @Bean
    // Chỉ khi app connect vào mysql thì mới khởi tạo bean này
    // Do muốn khi test ta dùng h2 db nên không thể tạo admin ngay lần đầu chạy đc
    // Nên khi test bean này ko đc khởi tạo
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(
            UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository) {

        return args -> {
            log.info("Init application start...............");

            // KHỞI TẠO CÁC PERMISSIONS CƠ BẢN TRƯỚC
            Permission createPost = permissionRepository
                    .findById("CREATE_POST")
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .name("CREATE_POST")
                            .description("Create a new post")
                            .build()));

            Permission deleteUser = permissionRepository
                    .findById("DELETE_USER")
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .name("DELETE_USER")
                            .description("Delete user")
                            .build()));

            // TẠO CÁC ROLES
            Role adminRole = roleRepository
                    .findById("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("ADMIN")
                            .description("Role admin")
                            .permissions(Set.of(createPost, deleteUser))
                            .build()));

            Role userRole = roleRepository
                    .findById("USER")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("USER")
                            .description("Role user")
                            .permissions(Set.of(createPost))
                            .build()));

            if (!userRepository.existsByUsername("ADMIN")) {
                User admin = User.builder()
                        .username(defaultAdminUsername)
                        .password(passwordEncoder.encode(defaultAdminPassword))
                        .roles(Set.of(adminRole))
                        .build();

                userRepository.save(admin);
                log.info("Create user admin successfully! Default password: " + defaultAdminPassword);
            }

            log.info("Init application end..................");
        };
    }
}
