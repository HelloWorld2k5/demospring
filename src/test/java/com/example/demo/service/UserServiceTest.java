package com.example.demo.service;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.response.PermissionResponse;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.mapper.UserMappper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

// Đối với Unit Test tầng Service, KHÔNG NÊN dùng @SpringBootTest nữa (vì nó load cả DB, Security lên rất chậm)
@ExtendWith(MockitoExtension.class) // dùng JUnit 5 kết hợp thuần Mockito chạy nhanh mất có vài ms
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMappper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks // tự động tiêm 4 mock fields kia vào userService
    private UserService userService;

    private UserCreationRequest userCreationRequest;
    private UserResponse userResponse;
    private RoleResponse roleResponse;
    private PermissionResponse permissionResponse;
    private User user;
    private Role role;
    private Permission permission;

    private LocalDate dob;

    @BeforeEach
    void initData() {
        dob = LocalDate.of(2005, Month.AUGUST, 21);

        userCreationRequest = UserCreationRequest.builder()
                .username("trg25")
                .password("12345678")
                .fullName("Nguyễn Thế Trưởng")
                .dob(dob)
                .build();

        permissionResponse = PermissionResponse.builder()
                .name("CREATE_POST")
                .description("Create a new post")
                .build();

        roleResponse = RoleResponse.builder()
                .name("USER")
                .description("Role user")
                .permissions(Set.of(permissionResponse))
                .build();

        userResponse = UserResponse.builder()
                .userId("d309f29v02940vjew")
                .username("trg25")
                .fullName("Nguyễn Thế Trưởng")
                .dob(dob)
                .roles(Set.of(roleResponse))
                .build();

        permission = Permission.builder()
                .name("CREATE_POST")
                .description("Create a new post")
                .build();

        role = Role.builder()
                .name("USER")
                .description("Role user")
                .permissions(Set.of(permission))
                .build();

        user = User.builder()
                .userId("d309f29v02940vjew")
                .username("trg25")
                .fullName("Nguyễn Thế Trưởng")
                .dob(dob)
                .roles(Set.of(role))
                .build();

    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(false);
        when(userMapper.toUser(ArgumentMatchers.any())).thenReturn(user);
        when(passwordEncoder.encode(ArgumentMatchers.anyString())).thenReturn("csampori2091rb3928dhsa");
        when(roleRepository.findById(ArgumentMatchers.anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(ArgumentMatchers.any())).thenReturn(user);
        when(userMapper.toUserResponse(ArgumentMatchers.any())).thenReturn(userResponse);

        // WHEN
        UserResponse response = userService.createUser(userCreationRequest);

        // THEN
        assertThat(response.getUserId()).isEqualTo("d309f29v02940vjew");
        assertThat(response.getUsername()).isEqualTo("trg25");
        assertThat(response.getFullName()).isEqualTo("Nguyễn Thế Trưởng");
        assertThat(response.getDob()).isEqualTo(dob);

    }

    @Test
    void createUser_userExsisted_fail() {
        // GIVEN
        when(userRepository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(true);

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> userService.createUser(userCreationRequest));

        // THEN
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1004);

    }

}
