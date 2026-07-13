package com.example.demo.configuration;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

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
        havingValue = "com.mysql.cj.jdbc.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {

        return args -> {
            log.info("Init application.............");

            // Nếu user admin chưa tồn tại tức là lần đầu app chạy
            if (userRepository.findByUsername("admin").isEmpty()) {
                // Set<String> roles = new HashSet<>();
                // roles.add(Role.ADMIN.name());

                Role role = roleRepository
                        .findById("ADMIN")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

                User user = User.builder()
                        .username(defaultAdminUsername)
                        .password(passwordEncoder.encode(defaultAdminPassword))
                        .roles(Set.of(role))
                        .build();

                if (user == null) {
                    log.error("Cannot create admin user! Some errors happen!");
                    return;
                }   

                userRepository.save(user);

                log.warn("Admin user created successfully with default password: admin2k5");
            }
        };
    }

}
