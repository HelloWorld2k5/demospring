package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @BeforeEach // chạy trước mỗi khi hàm test chạy
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

    @AfterEach // chạy khi mỗi lần chạy hàm test xong
    void cleanUp() {
        // đảm bảo dữ liệu đăng nhập giả lập của bài test này
        // không bị lây nhiễm sang bài test khác
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toUser(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("csampori2091rb3928dhsa");
        when(roleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

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
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // WHEN
        // Lấy exception trả về trong test case lỗi
        AppException exception = assertThrows(
                AppException.class, () -> userService.createUser(userCreationRequest)); // lỗi khi gọi hàm này

        // THEN
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1004);
    }

    @Test
    void createUser_roleNotFound_fail() {
        // GIVEN
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toUser(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("csampori2091rb3928dhsa");
        when(roleRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> userService.createUser(userCreationRequest));

        // THEN
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1010);
    }

    @Test
    // Trong hàm này ta có lấy username từ context, nên ta dùng WithMockUser từ
    // spring security test để mock
    // cái annotation này phải dùng SpringBootTest mới hoạt động được nhưng trong
    // đây ta đang dùng Unit test thuần Mockito
    // nên việc mock này ko hiểu quả và khi getName trong context chạy sẽ đọc null
    // => giải pháp là tự tạo authentication trong hàm test
    // @WithMockUser(username = "trg25")
    void getMyInfo_validRequest_success() {
        // GIVEN 1: Giả lập SecurityContextHolder để lấy được username "trg25"
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("trg25");
        SecurityContextHolder.setContext(securityContext);

        // GIVEN 2: Giả lập các tầng Repository và Mapper
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // THEN
        UserResponse response = userService.getMyInfo();

        // WHEN
        assertThat(response.getUserId()).isEqualTo("d309f29v02940vjew");
        assertThat(response.getUsername()).isEqualTo("trg25");
    }

    @Test
    void getMyInfo_userNotFound_fail() {
        // GIVEN
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("trg25");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(null));

        // WHEN
        // Lấy exception trả về khi test case user not found
        AppException exception = assertThrows(AppException.class, () -> userService.getMyInfo()); // khi gọi hàm này

        // THEN
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1006);
    }
}
