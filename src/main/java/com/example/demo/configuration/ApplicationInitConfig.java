package com.example.demo.configuration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
// import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Config lần đầu app chạy thì sẽ tạo 1 user admin duy nhất
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {

        return args -> {
            // Nếu user admin chưa tồn tại tức là lần đầu app chạy
            if (userRepository.findByUsername("admin").isEmpty()) {
                // Set<String> roles = new HashSet<>();
                // roles.add(Role.ADMIN.name());

                Set<Role> roles = new HashSet<>();
                roleRepository.findById("ADMIN").map(role -> roles.add(role));

                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin2k5"))
                        .roles(roles)
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
